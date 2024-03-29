package com.Data;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class Config implements Configs {
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private ArrayList<String> channelNames = new ArrayList<>();
    private String hostname = "192.168.0.1";
    private double frequencyStepWidth = 1000;
    private double voltageStepWidth = 0.01;
    private int startFrequency = 1000;
    private int port = 1337;
    private int blockSize = 512;
    private int highestValueOnY = 280;

    public int getHighestValueOnY() {
        return highestValueOnY;
    }

    public Config() {
        readConfigFile();
    }

    @Override
    public void writeConfigFile() {
        OutputStream fileWriter = null;
        JsonWriter jsonWriter = null;

        try {
            flushFile("config.json");
            fileWriter = new FileOutputStream("config.json");
            jsonWriter = Json.createWriter(fileWriter);
            jsonWriter.writeObject(createJsonObject());
            jsonWriter.close();
        } catch (IOException e) {
            System.out.println("\u001B[31m" + "ERROR: " + e.getMessage());
        } finally {
            try {
                close(fileWriter, jsonWriter);
            } catch (IOException e) {
                System.out.println("\u001B[31m" + "ERROR: " + e.getMessage());
            }
        }
    }

    @Override
    public void writeSelectedChannelsFile() {
        OutputStream fileWriter = null;
        JsonWriter jsonWriter = null;
        try {
            flushFile("selected.json");
            fileWriter = new FileOutputStream("selected.json");
            jsonWriter = Json.createWriter(fileWriter);
            JsonArrayBuilder array = Json.createArrayBuilder();
            for (int index = 0; index < selectedItems.size(); index++) {
                array.add(index, selectedItems.get(index));
            }
            JsonObject jsonObject = Json.createObjectBuilder().add("Selected Items", array).build();
            jsonWriter.writeObject(jsonObject);
        } catch (IOException e) {
            System.out.println("\u001B[31m" + "ERROR: " + e.getMessage());
        } finally {
            try {
                close(fileWriter, jsonWriter);
            } catch (IOException e) {
                System.out.println("\u001B[31m" + "ERROR: " + e.getMessage());
            }
        }
    }

    private void readConfigFile() {
        InputStream inputStream = null;
        JsonReader reader = null;
        InputStream inputStreamSelectedFile = null;
        JsonReader reader1 = null;
        try {
            inputStream = new FileInputStream("config.json");
            reader = Json.createReader(inputStream);
            inputStreamSelectedFile = new FileInputStream("selected.json");
            reader1 = Json.createReader(inputStreamSelectedFile);

            JsonObject selectedChannels = reader1.readObject();
            JsonObject config = reader.readObject();
            JsonArray selectedChannelsArray = selectedChannels.getJsonArray("Selected Items");

            for (JsonValue selectedChannelIndex : selectedChannelsArray) {
                this.selectedItems.add(Integer.parseInt(selectedChannelIndex.toString()));
            }

            this.startFrequency = config.getInt("StartFrequency");
            this.frequencyStepWidth = Double.parseDouble(config.getString("FrequencyStepWidth"));
            this.voltageStepWidth = Double.parseDouble(config.getString("VoltageStepWidth"));
            this.port = config.getInt("Port");
            this.hostname = config.getString("Hostname");
            this.blockSize = config.getInt("BlockSize");
            this.highestValueOnY = config.getInt("Highest Y Value");

            JsonArray channelNames = config.getJsonArray("channelNames");

            for (JsonValue channelName : channelNames) {
                this.channelNames.add(channelName.toString().substring(1, channelName.toString().length() - 1));
            }
            if (checkForSimilarNames()) {
                JOptionPane.showMessageDialog(null, "You have similar Channel names please correct that and restart the program", "An Error occurred", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (FileNotFoundException | JsonParsingException | NumberFormatException | NullPointerException e) {
            System.out.println("\u001B[31m" + "ERROR while Parsing Json file: " + e.getMessage());
        } finally {
            try {
                close(inputStream, inputStreamSelectedFile, reader, reader1);
            } catch (IOException e) {
                System.out.println("\u001B[31m" + "ERROR: " + e.getMessage());
            }
        }
    }

    @Override
    public void updateConfig(int startFrequency, double voltageStepWidth, int port, String hostname, double frequencyStepWidth, int highestValueOnY) {
        if (startFrequency >= 0) this.startFrequency = startFrequency;
        if (frequencyStepWidth >= 0) this.frequencyStepWidth = frequencyStepWidth;
        if (voltageStepWidth >= 0) this.voltageStepWidth = voltageStepWidth;
        if (port >= 0) this.port = port;
        this.highestValueOnY = highestValueOnY;
        this.hostname = hostname;
    }

    @Override
    public ArrayList<Integer> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public void setSelectedItems(ArrayList<Integer> selectedItems) {
        this.selectedItems = selectedItems;
    }

    @Override
    public ArrayList<String> getChannelNames() {
        return channelNames;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public double getFrequencyStepWidth() {
        return frequencyStepWidth;
    }

    @Override
    public double getVoltageStepWidth() {
        return voltageStepWidth;
    }

    @Override
    public int getStartFrequency() {
        return startFrequency;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getBlockSize() {
        return blockSize;
    }

    private JsonObject createJsonObject() {
        JsonArrayBuilder channelNames = Json.createArrayBuilder();
        int index = 0;
        for (String channelName : this.channelNames) {
            channelNames.add(index, channelName);
            index++;
        }
        return Json.createObjectBuilder()
                .add("StartFrequency", startFrequency)
                .add("FrequencyStepWidth", String.valueOf(frequencyStepWidth))
                .add("VoltageStepWidth", String.valueOf(voltageStepWidth))
                .add("channelNames", channelNames)
                .add("Hostname", hostname)
                .add("Port", port)
                .add("BlockSize", blockSize)
                .add("Highest Y Value", highestValueOnY)
                .build();
    }

    private boolean checkForSimilarNames() {
        for (int firstChannelIndex = 0; firstChannelIndex < channelNames.size(); firstChannelIndex++) {
            for (int secondChannelIndex = firstChannelIndex + 1; secondChannelIndex < channelNames.size(); secondChannelIndex++) {
                if (channelNames.get(firstChannelIndex).equals(channelNames.get(secondChannelIndex))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void flushFile(String name) {
        FileWriter fwOb = null;
        PrintWriter pwOb = null;

        try {
            fwOb = new FileWriter(name, false);
            pwOb = new PrintWriter(fwOb, false);
            pwOb.flush();
        } catch (IOException e) {
            System.out.println("ERROR:" + e.getMessage()); //?
        } finally {
            if (pwOb != null) pwOb.close();
            try {
                if (fwOb != null) fwOb.close();
            } catch (IOException e) {
                System.out.println("\u001B[31m" + "ERROR: " + e.getMessage());
            }
        }

    }

    private void close(OutputStream outputStream, JsonWriter writer) throws IOException {
        if (outputStream != null) outputStream.close();
        if (writer != null) writer.close();
    }

    private void close(InputStream inputStream, InputStream inputStream2, JsonReader jsonReader, JsonReader jsonReader2) throws IOException {
        if (inputStream != null) inputStream.close();
        if (inputStream2 != null) inputStream2.close();
        if (jsonReader != null) jsonReader.close();
        if (jsonReader2 != null) jsonReader2.close();
    }
}
