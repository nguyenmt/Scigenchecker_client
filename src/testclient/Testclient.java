/*
 * Copyright (C) 2015 Nguyen Minh Tien - minh-tien.nguyen@imag.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package testclient;

import com.sun.prism.impl.PrismSettings;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author tien
 */
public class Testclient {

    private Double thresshold_sci;
    private Double thresshold_sup;
    private String logtime;

    /**
     * @param args the command line arguments
     */
    private void readfolder(String foldername) throws IOException {

        File folder = new File(foldername);
        File[] listOfFile = folder.listFiles();

        for (int j = 0; j < listOfFile.length; j++) {
            // System.out.println(listOfFile[j].getName());
            // read subfolders
            if (listOfFile[j].isDirectory()) {
                readfolder(listOfFile[j].getPath());
            } // if its a pdf file; in future can make another elseif for xml file
            else if (listOfFile[j].getName().endsWith(".pdf")) {
                String fileName = listOfFile[j].getName();
                File file = new File(listOfFile[j].getPath());
                process(file);

            }
        }
    }

    private void process(File pdf) throws IOException {
        try {
            FileInputStream fis = new FileInputStream(pdf);
            BufferedInputStream inputStream = new BufferedInputStream(fis);
            byte[] imageBytes = new byte[(int) pdf.length()];
            inputStream.read(imageBytes);
            // upload returns the distant to the NN
            String result = upload(pdf.getName(), imageBytes);

            inputStream.close();
            File fileresult = new File("results/" + logtime + ".xls");
            FileWriter fw = new FileWriter(fileresult, true); //the true will append the new data
            fw.write(pdf.getPath() + "\t" + classify(result) + "\n");
            // fw.write();//appends the string to the file
            fw.close();
            System.out.println(result);

            //Save the all distant if needed (define latter via argument)
            File alldistant = new File("logs/" + logtime + ".xls");
            FileWriter fw2 = new FileWriter(alldistant, true); //the true will append the new data
            fw2.write(pdf.getPath() + "\n");
            fw2.write(downloadresult());//appends the string to the file
            fw2.close();

        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    private String classify(String result) {
        try {
            String[] part = result.split("\t");
            if (Double.parseDouble(part[2]) < thresshold_sci) {
                return gettype(part[1]) + "\t" + part[2] + "\t" + part[1];
            }
            if (Double.parseDouble(part[2]) < thresshold_sup) {
                return "supected " + gettype(part[1]) + "\t" + part[2] + "\t" + part[1];
            }

            return "Genuine\t" + part[2] + "\t" + part[1];
        } catch (Exception e) {
            return "Cant classify\t" + result;
        }

    }

    private String gettype(String path) {
        path = path.substring(0, path.lastIndexOf("/") - 1);

        return path.substring(path.lastIndexOf("/"), path.length());
    }

    public static void main(String[] args) throws IOException {
        String path = args[0];
        Testclient a = new Testclient();
        try {
            File conf = new File("Config.txt");
            BufferedReader br = new BufferedReader(new FileReader(conf));
            // HashMap<String, Double> a = new HashMap<String, Double>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    System.out.println(line);
                    String[] b = line.split("\t");
                    if (b[0].equals("threshold_Sci")) {
                        a.thresshold_sci = Double.parseDouble(b[1]);
                    }
                    if (b[0].equals("threshold_Suspect")) {
                        a.thresshold_sup = Double.parseDouble(b[1]);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error reading Config.txt, use default");
            a.thresshold_sci = 0.48;
            a.thresshold_sup = 0.56;
        }
        DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        Date date = new Date();
        a.logtime = dateFormat.format(date);
        a.readfolder(path);
    }

    private static String downloadresult() {
        l.Checker_Service service = new l.Checker_Service();
        l.Checker port = service.getCheckerPort();
        return port.downloadresult();
    }

    private static String upload(java.lang.String arg0, byte[] arg1) {
        l.Checker_Service service = new l.Checker_Service();
        l.Checker port = service.getCheckerPort();
        return port.upload(arg0, arg1);
    }

 

}
