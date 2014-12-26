package nisargpatel.inertialnavigation.javacode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFile {

    private FileWriter fw;
    private BufferedWriter bf;

    private String folderPath;

    public WriteFile(String folderPath) throws IOException {
        this.folderPath = folderPath;

        fw = new java.io.FileWriter(folderPath + "\\" + getFileName());
        bf = new BufferedWriter(fw);
    }

    private String getFileName() throws IOException {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        boolean fileFound;
        int fileCount = 0;

        do {

            fileCount++;

            fileFound = false;

            for (int i = 1; i <= listOfFiles.length; i++) {

                //array starts at 0
                if (listOfFiles[i - 1].getName().equals("accData" + fileCount + ".txt")) {
                    fileFound = true;
                }

            }

        } while (fileFound == true);

        return "accData" + fileCount + ".txt";

		/*
        int fileCount = 0;

        //going through all possible files in folder
        for (int i = 0; i < listOfFiles.length; i++) {

            //going through all file names possible with current folder size
            for (int k = 0; k < listOfFiles.length; k++) {

                if (listOfFiles[i].getName().equals("accData" + k)) {
                    fileCount++;
                }
            }

        }

        return "accData" + fileCount++;
        */

    }

    public void writeToFile(String output) throws IOException {
        bf.write(output);
    }

    public void closeWriter() throws IOException {
        bf.close();
    }

}
