/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Shreyansh
 */
public class AlphaMap 
{
    int[][] iaAlphaMap;
    String szAlphaMap;
    int iH, iW;
    
    public AlphaMap()
    {
        //Def Constuctor;
    }
    
    //Use this to read the alpha channel file
    public void ReadAlphaMap(String szFile, int iW, int iH)
    {
        try 
        {
            File file = new File(szFile);
            InputStream is = new FileInputStream(file);

            long len = file.length();
            byte[] bytes = new byte[(int) len];

            int offset = 0;
            int numRead = 0;

            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) 
            {
                offset += numRead;
            }

            int ind = 0;
            
            for (int y = 0; y < iH; y++) 
            {
                for (int x = 0; x < iW; x++) 
                {                   
                    byte B = bytes[ind];                    
                    
                    this.iaAlphaMap[x][y] = (int) B;
                    ind++;                    
                }
            }
        } 
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    //Use this Function for Comparing with Image
    public int Alpha(int iX, int iY)
    {
        return this.iaAlphaMap[iX][iY];
    }
}
