package vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Operations with files
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.swing.filechooser.FileNameExtensionFilter;

import vava33.d1dplot.D1Dplot_global;

import com.vava33.jutils.VavaLogger;
import com.vava33.jutils.FileUtils;

public final class DataFileUtils {

    private static VavaLogger log = D1Dplot_global.log;

    public static enum SupportedReadExtensions {DAT,XYE,XY;}
    public static enum SupportedWriteExtensions {DAT,XYE;}
    public static final Map<String, String> formatInfo;
    static
    {
        formatInfo = new HashMap<String, String>(); //ext, description
        formatInfo.put("dat", "2 or 3 columns file 2th/int/(err) (*.dat)");
        formatInfo.put("xye", "3 columns file 2th/int/err (*.xye)");
        formatInfo.put("xy", "2 columns file 2th/int (*.xy)");
    }
    
    
    public static FileNameExtensionFilter[] getExtensionFilterWrite(){
        //mirem quins formats som capaços de salvar segons ImgFileUtils
        Iterator<String> itrformats = DataFileUtils.formatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedWriteExtensions.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            String frm = itrformats.next();
            //this line returns the FORMAT in the ENUM or NULL
            DataFileUtils.SupportedWriteExtensions wfrm = FileUtils.searchEnum(DataFileUtils.SupportedWriteExtensions.class, frm);
            if (wfrm!=null){
                //afegim filtre
                filter[nfiltre] = new FileNameExtensionFilter(DataFileUtils.formatInfo.get(frm), frm);
                nfiltre = nfiltre +1;
            }
        }
        return filter;
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterRead(){
        //mirem quins formats som capaços de salvar segons ImgFileUtils
        Iterator<String> itrformats = DataFileUtils.formatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedReadExtensions.values().length+1]; //+1 for all image formats
        String[] frmStrings= new String[DataFileUtils.SupportedReadExtensions.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            String frm = itrformats.next();
            //this line returns the FORMAT in the ENUM or NULL
            DataFileUtils.SupportedReadExtensions wfrm = FileUtils.searchEnum(DataFileUtils.SupportedReadExtensions.class, frm);
            if (wfrm!=null){
                //afegim filtre
                filter[nfiltre] = new FileNameExtensionFilter(DataFileUtils.formatInfo.get(frm), frm);
                frmStrings[nfiltre] = frm;
                nfiltre = nfiltre +1;
            }
        }
        //afegim filtre de tots els formats
        filter[nfiltre] = new FileNameExtensionFilter("All 1D-XRD supported formats", frmStrings);
        return filter;
    }
    
    private static boolean isComment(String ln){
        if (ln.trim().startsWith("#"))return true;
        if (ln.trim().startsWith("!"))return true;
        if (ln.trim().startsWith("/"))return true;
        if (ln.trim().startsWith("$"))return true;
        return false;
    }
    
    public static Pattern1D readDAT(File datFile) {
        Pattern1D patt1D = new Pattern1D(); //create an empty pattern1D
        boolean firstLine = true;
        boolean readed = true;
        try {
            Scanner sf = new Scanner(datFile);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    patt1D.getCommentLines().add(line);
                    continue;
                }
                if (line.trim().isEmpty()){
                    continue;
                }

                String values[] = line.trim().split("\\s+");

                float t2 = Float.parseFloat(values[0]);
                float inten = Float.parseFloat(values[1]);
                float sdev = 0.0f;
                try{
                    sdev = Float.parseFloat(values[2]);
                }catch(Exception ex){
                    //ex.printStackTrace();
                }

                patt1D.getPoints().add(new DataPoint(t2,inten,sdev));
                if (firstLine){
                    patt1D.setT2i(t2);
                    firstLine = false;
                }

                if (!sf.hasNextLine()){
                    patt1D.setT2f(t2);
                }
            }
            sf.close();

        }catch(Exception e){
            e.printStackTrace();
            readed = false;
        }
        if (readed){
            return patt1D;
        }else{
            return null;
        }
    }
}
