/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package project_final;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.System.exit;
import javax.swing.*;
import sun.awt.im.InputMethodJFrame;
import Image.Image;
import Recognizer.Recognizer;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.*;

import Image.KMeans;
import Recognizer.Recognizer;
/**
 *
 * @author Shreyansh
 */
public class Project_Final 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {               
        String strDB = null, strQ = null, strAlpha = null;
        
        if(args.length == 2)
        {
            strDB = args[1];
            strQ = args[0];            
        }
        else if(args.length == 3)
        {
            strDB = args[1];
            strQ = args[0];
            strAlpha = args[2];
        }
        else
        {
            System.out.println("Arguments Error.!");
            exit(1);
        }        
        
        // TODO code application logic here
        JFrame frmDB = new JFrame();
        JFrame frmQ = new JFrame();                                  
        JFrame frmRes1 = new JFrame();
        JFrame frmRes2 = new JFrame();
        JFrame frmRes3 = new JFrame();
        
        //All Objects
        Recognizer rg = new Recognizer();        
        KMeans k = new KMeans();
        
        
        Image imQ = new Image();
        Image imQL1;
        Image imQL2;
        Image imQL3;
        
        Image imDB = new Image();                
        Image imResL1;
        Image imResL2;
        Image imResL3;
                
        //Init Functions                                    
        imQ.ReadImage(strQ, 288, 352);
        if(args.length == 3)
            imQ.ReadAlpha(strAlpha, 352, 288);
        
        //Resample and scale down different versions of Query Image        
        imQL1 = imQ.Pyramidal_Resampling(2, 2, 352, 288);
        imQL2 = imQ.Pyramidal_Resampling(3, 3, 352, 288);
        imQL3 = imQ.Pyramidal_Resampling(4, 4, 352, 288);
        
        //Read the DataBase Image within which the search is to be performed
        imDB.ReadImage(strDB, 288, 352);        
        
        //Template Matching
        imResL1 = rg.TemplateMatching(imQL1, imDB, Imgproc.TM_CCORR);
        imResL2 = rg.TemplateMatching(imQL2, imDB, Imgproc.TM_CCORR);
        imResL3 = rg.TemplateMatching(imQL3, imDB, Imgproc.TM_CCORR);                
        
        
        //HistMatch OpenCV
        //imResL1 = rg.HistBlockCompare(imQ, imDB, 100, 100);
                
        frmQ.setLocation(10, 10);
        frmDB.setLocation(410, 10);
        frmRes1.setLocation(810, 10);
        frmRes2.setLocation(10, 350);
        frmRes3.setLocation(410, 350);
        
        imQ.DisplayImage(frmQ);
        imDB.DisplayImage(frmDB);
        
        imResL1.DisplayImage(frmRes1);
        imResL2.DisplayImage(frmRes2);
        imResL3.DisplayImage(frmRes3);
        
        System.out.println("Match Found Yes.!");
        
        //Other Method tried and tested
        //rg.HistMatch(imQL3, imDB);
        //rg.Calculate_Histogram_Hue_Bins(imQ, fHueHist, BinSize);
        //rg.Mean_Shift_Segmentation(imDB);
        //rg.SIFT(imQ, imDB);
        //rg.Recognize_Logo_using_HMap(imQ, imDB);
        //rg.Histogram_Analysis(imQL3, imDB, BinSize, BlockSize);       
    }
    
    public static void DrawRect(int iX, int iY, Image img)
    {
        for(int i = 0 ; i < 352 ; i ++)
        {
            for(int j = 0 ; j < 288 ; j ++)
            {
                if(i <= iX + 90 && i >= iX - 10 && j <= iY + 90 && j >= iY - 10)
                {
                    img.imgI.setRGB(i, j, 0x0000FF00);
                }
            }
        }
    }
}
