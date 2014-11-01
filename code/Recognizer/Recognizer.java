/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Recognizer;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.System.exit;
import javax.swing.*;
import sun.awt.im.InputMethodJFrame;
import org.opencv.core.*;
import org.opencv.core.Core;
import static org.opencv.core.CvType.CV_32SC3;
import Image.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import  org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;

/**
 *
 * @author Shreyansh
 */
public class Recognizer 
{    
    private int iNumOfImages;
    private int iScore;
    private Image imIP;
    private Image[] imgDatabase;
    private int iNumOfImgDB;
    
    public void ReadImageDataBase()
    {                 
        File file = new File("E:/imgDB.txt");
        
        InputStream is = null; 
        InputStreamReader isr = null;
        BufferedReader br = null;
                        
        try//(BufferedReader br = new  BufferedReader(new FileReader(file)))
        {
            is = new FileInputStream("imgDB.txt");
            isr = new InputStreamReader(is);
            
            br = new BufferedReader(isr);
            
            String line = br.readLine();
            
            this.iNumOfImgDB = Integer.parseInt(line);
            
            this.imgDatabase = new Image[this.iNumOfImgDB];
            int i = 0;
            
            while(i<this.iNumOfImgDB)
            {
                line = br.readLine();
                this.imgDatabase[i] = new Image(line, 288, 352); //ReadImage(line, 288, 352);
                i++;
            }
        }
        catch(Exception e)
        {
            
        }
        
        System.out.println("Image Database Read Complete.!");
    }
    
    void ReadInputImage()
    {
        
    }
    
    public Image TemplateMatching (Image imQuery, Image imDB, int match_method)
    {               
        System.out.println("Running Template Matching ...");
        
        //Mat img = Highgui.imread(inFile); // Image in which area has to be searched
        //Mat template_img = Highgui.imread(templateFile); // Search Image
                
        Mat matQuery = imQuery.Image3CtoMat_CV();        
        Mat matDB = imDB.Image3CtoMat_CV();
        
        Mat hsvQ = new Mat(), hsvDB = new Mat();
        
        Imgproc.cvtColor(matQuery, hsvQ, COLOR_RGB2HSV);
        Imgproc.cvtColor(matDB, hsvDB, COLOR_RGB2HSV);
                        
        // Create result image matrix
        int resultImg_cols = matDB.cols() - matQuery.cols() + 1;
        int resultImg_rows = matDB.rows() - matQuery.rows() + 1;
        
        Mat matRes = new Mat(resultImg_rows, resultImg_cols, CvType.CV_32FC1);
        
        // Template Matching with Normalization
        Imgproc.matchTemplate(hsvDB, hsvQ, matRes, match_method);
        Core.normalize(matRes, matRes, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        
        // / Localizing the best match with minMaxLoc
        Core.MinMaxLocResult Location_Result = Core.minMaxLoc(matRes);
        Point matchLocation;
        
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED)
        {
            matchLocation = Location_Result.minLoc;            
        }
        else 
        {
            matchLocation = Location_Result.maxLoc;
        }
        
        // Display Area by Rectangle
        Core.rectangle(matDB, matchLocation, new Point(matchLocation.x + matQuery.cols(), matchLocation.y + matQuery.rows()), new Scalar(0, 255, 0));
        
        Image imOut = new Image(matDB.width(), matDB.height());
        //Image imOut = new Image(matQuery.cols(), matQuery.rows());
        
        //Mat m = new Mat(matDB);
        
        //m =//matDB.submat((int)matchLocation.y, (int)matchLocation.y + matQuery.rows(),(int)matchLocation.x, (int)matchLocation.x + matQuery.cols());
        
        imOut.Mat_CVtoImage3C(matDB);
        
        System.out.println("Location: " + Location_Result.minLoc.x + " " + Location_Result.minLoc.y + "   " + Location_Result.maxLoc.x + " " + Location_Result.maxLoc.y);
        
        return imOut;
    }
    
    public Image HistMatch(Image imQuery, Image imDB)
    {
        Image imOut = new Image(352, 288);
        
        Mat srcQ, srcDB;
        Mat hsvQ = new Mat(), hsvDB = new Mat();        
                        
        srcQ = imQuery.Image3CtoMat_CV();
        srcDB = imDB.Image3CtoMat_CV();
        
        //Convert To HSV
        Imgproc.cvtColor(srcQ, hsvQ, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(srcDB, hsvDB, Imgproc.COLOR_RGB2HSV);
        
        java.util.List<Mat> matlistQ = Arrays.asList(hsvQ);
        java.util.List<Mat> matlistDB = Arrays.asList(hsvDB);
                               
        //Use 100 bins for hue, 100 for Saturation
        int h_bins = 360, s_bins = 4;
        int[] histsize = {h_bins, s_bins};
        MatOfInt histSize = new MatOfInt(histsize);
        
        MatOfFloat Ranges = new MatOfFloat(0, 180, 0, 256);
        
        int[] channels = {0, 1};
        MatOfInt CH = new MatOfInt(channels);
        
        Mat hist_Q = new Mat();
        Mat hist_DB = new Mat();
        
        Imgproc.calcHist(matlistQ, CH, new Mat(), hist_Q, histSize, Ranges);
        Core.normalize(hist_Q, hist_Q, 0, 1,Core.NORM_MINMAX, -1, new Mat());
        
        float res;
        
        Mat[] hsvaLev1 = new Mat[4];
        Mat[] hsvaLev2 = new Mat[16];
        Mat[] hsvaLev3 = new Mat[64];
       // Mat[] hsvaLev4 = new Mat[256];
        
        float[] iaLev1 = new float[4];
        float[] iaLev2 = new float[16];
        float[] iaLev3 = new float[64];
        //float[] iaLev4 = new float[256];
                
        for(int i = 0;i <2 ;i++)
        {
            for(int j = 0; j< 2; j++)
            {
                hsvaLev1[i*2 + j] = hsvDB.submat(0 + i*288/2, 143 + i*288/2, 0 + j*352/2, 175 + j*352/2);
            }
        }
        
        for(int i = 0;i < 4 ;i++)
        {
            for(int j = 0; j< 4; j++)
            {
                hsvaLev2[i*4+j] = hsvDB.submat(0 + i*288/4, 71 + i*288/4, 0 + j*352/4, 87 + j*352/4);
            }
        }
        
        for(int i = 0;i < 8 ;i++)
        {
            for(int j = 0; j< 8; j++)
            {
                hsvaLev3[i*8+j] = hsvDB.submat(0 + i*288/8, 35 + i*288/8, 0 + j*352/8, 43 + j*352/8);
            }
        }                
        
        System.out.println("Lev_1");
        for(int m = 0;m<4;m++)
        {
            matlistDB = Arrays.asList(hsvaLev1[m]);
            Imgproc.calcHist(matlistDB, CH, new Mat(), hist_DB, histSize, Ranges);
            Core.normalize(hist_DB, hist_DB, 0, 1,Core.NORM_MINMAX, -1, new Mat());
            res = (float) Imgproc.compareHist(hist_Q, hist_DB, Imgproc.CV_COMP_BHATTACHARYYA);
            
            System.out.println("Res: " + res);
            iaLev1[m] =  res;        
        }
        
        System.out.println("Lev_2");
        for(int m = 0;m<16;m++)
        {
            matlistDB = Arrays.asList(hsvaLev2[m]);
            Imgproc.calcHist(matlistDB, CH, new Mat(), hist_DB, histSize, Ranges);                
            Core.normalize(hist_DB, hist_DB, 0, 1,Core.NORM_MINMAX, -1, new Mat());
            res = (float) Imgproc.compareHist(hist_Q, hist_DB, Imgproc.CV_COMP_BHATTACHARYYA);
            
            System.out.println("Res: " + res);
            iaLev2[m] =  res;
        }
        
        System.out.println("Lev_3");
        for(int m = 0;m<64;m++)
        {
            matlistDB = Arrays.asList(hsvaLev3[m]);
            Imgproc.calcHist(matlistDB, CH, new Mat(), hist_DB, histSize, Ranges);                
            Core.normalize(hist_DB, hist_DB, 0, 1,Core.NORM_MINMAX, -1, new Mat());
            res = (float) Imgproc.compareHist(hist_Q, hist_DB, Imgproc.CV_COMP_BHATTACHARYYA);
            
            System.out.println("Res: " + res);
            iaLev3[m] =  res;
        }               
                               
        int x = MinIndex(iaLev1);
        int i = x%2;
        int j = x/2;     
        Core.rectangle(srcDB, new Point( 0 + j*352/2, 0 + i*288/2), new Point(175 + j*352/2, 143 + i*288/2), new Scalar(0, 255 ,0));
        
        x = MinIndex(iaLev2);
        i = x%4;
        j = x/4;
        Core.rectangle(srcDB, new Point( 0 + j*352/4, 0 + i*288/4), new Point(87 + j*352/4, 71 + i*288/4), new Scalar(0, 0 ,255));
        
        x = MinIndex(iaLev3);
        i = x%8;
        j = x/8;
        Core.rectangle(srcDB, new Point(0 + j*352/8, 0 + i*288/8), new Point(43 + j*352/8, 35 + i*288/8), new Scalar(255, 0 ,0));
        
        imOut.Mat_CVtoImage3C(srcDB);
        
        return imOut;
    }   
    
    int MinIndex(float[] Arr)
    {
        int i = 0; 
        float fMin = Arr[0];
        
        for(int j = 0;j <Arr.length; j++)
        {
            if(fMin > Arr[j])
            {
                i = j;
                fMin = Arr[j];
            }
        }
        
        return i;
    }
    
    public Image HistBlockCompare (Image imQuery, Image imDB,int m, int n) // SingleBlock Size mxn -> Eg: 88x72 -> m =88; n = 72
    {
        // Initialzations 
        Image imOut = new Image(352, 288);
        
        Mat srcQ, srcDB;
        Mat hsvQ = new Mat(), hsvDB = new Mat();        
                        
        srcQ = imQuery.Image3CtoMat_CV();
        srcDB = imDB.Image3CtoMat_CV();
        
        //Convert To HSV
        Imgproc.cvtColor(srcQ, hsvQ, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(srcDB, hsvDB, Imgproc.COLOR_RGB2HSV);
        
        java.util.List<Mat> matlistQ = Arrays.asList(hsvQ);
        java.util.List<Mat> matlistDB = Arrays.asList(hsvDB);
                               
        //Use 100 bins for hue, 100 for Saturation
        int h_bins = 180, s_bins = 2;
        int[] histsize = {h_bins, s_bins};
        MatOfInt histSize = new MatOfInt(histsize);
        
        MatOfFloat Ranges = new MatOfFloat(0, 180, 0, 256);
        
        int[] channels = {0, 1};
        MatOfInt CH = new MatOfInt(channels);
        
        Mat hist_Q = new Mat();
        Mat hist_DB = new Mat();
        
        Imgproc.calcHist(matlistQ, CH, new Mat(), hist_Q, histSize, Ranges);
        Core.normalize(hist_Q, hist_Q, 0, 1,Core.NORM_MINMAX, -1, new Mat());                
                
        float[][] CompareHistResult = new float[352-m][288-n];
        
        for(int i=0;i<(352-m); i++) // width
        {
            for (int j=0;j<(288-n);j++) // height
            {
                // Get Indiaviadua Submatrix for Matching putrposes
                hist_DB = hsvDB.submat(j,(j+n),i,(i+m));                
                // Now Compare Histogram using OpenCV functions
                matlistDB = Arrays.asList(hist_DB);
                Imgproc.calcHist(matlistDB, CH, new Mat(), hist_DB, histSize, Ranges);
                Core.normalize(hist_DB, hist_DB, 0, 1, Core.NORM_MINMAX, -1, new Mat());
                CompareHistResult[i][j] = (float) Imgproc.compareHist(hist_Q, hist_DB, Imgproc.CV_COMP_CHISQR);                                
            }
        }
        
        // Search min from result
        float min = CompareHistResult[0][0];
        int minIndex_i=0;
        int minIndex_j=0;
        for(int i=0;i<(352-m); i++) // width
        {
            for (int j=0;j<(288-n);j++) // height
            {
                if(CompareHistResult[i][j] < min)
                {
                    min = CompareHistResult[i][j];
                    minIndex_i = i;
                    minIndex_j = j;
                }
            }            
        }        
        // 
        Core.rectangle(srcDB, new Point(minIndex_i, minIndex_j), new Point(minIndex_i+m,minIndex_j+n), new Scalar(0, 255 ,0));
        
        System.out.println("Result: " + CompareHistResult[minIndex_i][minIndex_j]);
        imOut.Mat_CVtoImage3C(srcDB);
        
        return imOut;
    }
    
    int maximum(int num1,int num2)
    {
	if(num1>num2)
		return num1;
	else
        return num2;
    }
    
    int minimum(int num1,int num2)
    {
	if(num2<num1)
		return num2;
	else
        return num1;
    }

    public Image Mean_Shift_Segmentation (Image imDB)
    {
        Image imOut = new Image (352,288);
        
        int x=0;int y=0; int z=0;
        int cnt =0;
        int sigmaS = 15;
        int sigmaR = 18;
        int Kernel_Radius =  3*sigmaS;
        int temp = Kernel_Radius;
        
        int Kernel_Size = 2*(3*sigmaS)+1;
        double sigmaSsq = 2.0*sigmaS*sigmaS;
        double sigmaRsq = 2.0*sigmaR*sigmaR;
        double []kernel = new double[Kernel_Size*Kernel_Size];
        
        // SPATIAL DISTANCE KERNEL
        for(int i1=(-temp);i1<=(temp);i1++)
        {
            for(int j1=(-temp);j1<=(temp);j1++)
            {
                kernel[z] = Math.exp(-((i1*i1)+(j1*j1))/(sigmaSsq));
                z=z+1;
            }
        }
        
        // INITAILAZATIONS
        int StoppingCondition = 0;
        int IterationCount = 0;
        double WeightAccumulatedR = 0;
        double WeightAccumulatedG = 0;
        double WeightAccumulatedB = 0;
        double yAccumulatedR = 0;
        double yAccumulatedG = 0;
        double yAccumulatedB = 0;
        double SpatialWeight,IntensityWeight,WeightWindowR,WeightWindowG,WeightWindowB,yWindowR,yWindowG,yWindowB;
        double MeanSqError=0;
        double TotalDeviation = 0;
        int Vmax,Vmin,Hmax,Hmin;
        int yR,yG,yB,xWindowR,xWindowG,xWindowB,xDifferenceR,xDifferenceG,xDifferenceB;
        
        //// LOOP OVER IMAGE AND START MSE
        int height = 288;
        int width = 352;
        for(int i=0;i<width;i++)
        {   
            System.out.println("Progress:"+i);
            for(int j=0;j<height;j++)
            {
                WeightAccumulatedR=0;yAccumulatedR = 0;
                WeightAccumulatedG=0;yAccumulatedG = 0;
                WeightAccumulatedB=0;yAccumulatedB = 0;

                yR = (imDB.imgI.getRGB(i,j)>>16) & 0xFF;
                yG = (imDB.imgI.getRGB(i,j)>>8) & 0xFF;                
                yB = imDB.imgI.getRGB(i,j) & 0xFF;

                MeanSqError = 0;
                IterationCount = 0;
                StoppingCondition = 0;

                // CHECK FOR STOPPING CONDITION
                while (StoppingCondition == 0)
                {
                    // DEFINE IMAGE BOUNDARIES
                    Hmax = minimum(width,(i+Kernel_Radius)); //cout<<"Hmax : "<<Hmax<<endl; // width Original
                    Hmin = maximum(0,(i-Kernel_Radius)); //cout<<"Hmin  : "<<Hmin<<endl;
                    Vmax = minimum(height,(j+Kernel_Radius)); //cout<<"Vmax : "<<Vmax<<endl; //// height Original
                    Vmin = maximum(0,(j-Kernel_Radius)); //cout<<"Vmin  : "<<Vmin<<endl;

                    cnt = 0;
                    WeightWindowR = 0; //X
                    WeightWindowG = 0;
                    WeightWindowR = 0;
                    yWindowR = 0; //Y
                    yWindowG = 0;
                    yWindowB = 0;

                    for(int hor = Hmin; hor<Hmax; hor++) // Vert Original
                    {    //cout<<"WinV : "<<vert<<endl;
                        for (int vert = Vmin; vert<Vmax ; vert++) // Hor Original
                        {   //cout<<"WinH : "<<hor<<endl;
                            //cout<<"Count : "<<cnt<<endl;
                            SpatialWeight = kernel[cnt];
                            cnt = cnt + 1;

                            xWindowR = (imDB.imgI.getRGB(hor,vert)>>16) & 0xFF; //(or vert,hor?? - HOR VERT CORRECT)//Imagedata [(vert*width*BytesPerPixel)+(hor*BytesPerPixel)+0]; //cout<<(int)xWindowR<<endl;
                            xWindowG = (imDB.imgI.getRGB(hor,vert)>>8) & 0xFF;//Imagedata [(vert*width*BytesPerPixel)+(hor*BytesPerPixel)+1];
                            xWindowB = (imDB.imgI.getRGB(hor,vert)) & 0xFF;//Imagedata [(vert*width*BytesPerPixel)+(hor*BytesPerPixel)+2];

                            xDifferenceB =  Math.abs(yB - xWindowB);
                            xDifferenceG =  Math.abs(yG - xWindowG);
                            xDifferenceR =  Math.abs(yR - xWindowR);

                            TotalDeviation = (xDifferenceB*xDifferenceB)+(xDifferenceR*xDifferenceR)+(xDifferenceG*xDifferenceG);

                            // NOW CALCULATE INTENSITY KERNEL
                            IntensityWeight = Math.exp(-((TotalDeviation))/(sigmaRsq));

                            // ASSIGN WEIGHTS TO PIXELS IN WINDOW
                            WeightWindowR = (IntensityWeight)*(SpatialWeight)*(xDifferenceR);
                            WeightWindowG = (IntensityWeight)*(SpatialWeight)*(xDifferenceG);
                            WeightWindowB = (IntensityWeight)*(SpatialWeight)*(xDifferenceB);

                            // ADD THE ACCUMULATED WEIGHTS
                            yAccumulatedR = (yAccumulatedR) + ((xWindowR)*(WeightWindowR)) ;
                            yAccumulatedG = (yAccumulatedG) + ((xWindowG)*(WeightWindowG))  ;
                            yAccumulatedB = (yAccumulatedB) + ((xWindowB)*(WeightWindowB));

                            WeightAccumulatedR = (WeightAccumulatedR) + (WeightWindowR);
                            WeightAccumulatedG = (WeightAccumulatedG) + (WeightWindowG);
                            WeightAccumulatedB = (WeightAccumulatedB) + (WeightWindowB);

                        }
                    }
                    //cout<<"Outside Window :"<<endl;
                    yWindowR = (yAccumulatedR)/(WeightAccumulatedR);
                    yWindowG = (yAccumulatedG)/(WeightAccumulatedG);
                    yWindowB = (yAccumulatedB)/(WeightAccumulatedB);

                    MeanSqError = (((yWindowR-yR)*(yWindowR-yR))+((yWindowG-yG)*(yWindowG-yG))+((yWindowB-yB)*(yWindowB-yB)))/(3.0f);

                    if ((MeanSqError<0.01f)||(IterationCount>30))
                    {
                        StoppingCondition = 1;
                        //cout<<"#Iter :"<<IterationCount<<endl;
                    }
                    else
                    {
                        IterationCount = IterationCount+1;
                        yR = (int)yWindowR;
                        yG = (int)yWindowG;
                        yB = (int)yWindowB;
                        //cout<<"#Iter :"<<IterationCount<<endl;
                    }
                }
                // WRITE INTO OUTPUT IMAGE
                int pix = 0xff000000 | ((yR & 0xff) << 16) | ((yG & 0xff) << 8) | (yB & 0xff); 
                imOut.imgI.setRGB(i,j,pix);
                //OutputImage[(i*width*BytesPerPixel)+(j*BytesPerPixel)+0]=yR;
                //OutputImage[(i*width*BytesPerPixel)+(j*BytesPerPixel)+1]=yG;
                //OutputImage[(i*width*BytesPerPixel)+(j*BytesPerPixel)+2]=yB;

            }
        }
        
        return imOut;
    }
    
    public void SIFT(Image imQ, Image imDB)
    {                
        Mat Q = imQ.Image1CtoMat_CV();
        Mat DB = imDB.Image1CtoMat_CV();
        
        Mat matQ = new Mat();
        Mat matDB = new Mat();
        
        Q.convertTo(matQ, CvType.CV_8U);
        DB.convertTo(matDB,CvType.CV_8U);
        
        FeatureDetector siftDet = FeatureDetector.create(FeatureDetector.SIFT);
        DescriptorExtractor siftExt = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        
        MatOfKeyPoint kpQ = new MatOfKeyPoint();
        MatOfKeyPoint kpDB = new MatOfKeyPoint();
        
        siftDet.detect(matQ, kpQ);
        siftDet.detect(matDB, kpDB);
        
        Mat matDescriptorQ = new Mat(matQ.rows(), matQ.cols(), matQ.type());
        Mat matDescriptorDB = new Mat(matDB.rows(), matDB.cols(), matDB.type());
        
        siftExt.compute(matQ, kpQ, matDescriptorQ);
        siftExt.compute(matDB, kpDB, matDescriptorDB);
        
        MatOfDMatch matchs = new MatOfDMatch();
        
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        
        matcher.match(matDescriptorQ, matDescriptorDB, matchs);
        
        int N = 10;
        
        DMatch[] tmp01 = matchs.toArray();
        DMatch[] tmp02 = new DMatch[N];
        
        for(int i = 0;i<tmp02.length;i++)
        {
            tmp02[i] = tmp01[i];
        }
        
        matchs.fromArray(tmp02);
        
        Mat matchedImage = new Mat(matQ.rows(), matQ.cols()*2, matQ.type());
        Features2d.drawMatches(matQ, kpQ, matDB, kpDB, matchs, matchedImage);

		// 出力画像 at SIFT
        Highgui.imwrite("./descriptedImageBySIFT.jpg", matchedImage);
        
    }              
    
    public int[] Calculate_Histogram_Hue_Bins (Image im, int[] fHueHist, int BinSize)
    {
        // Divide the Histograms into bins
        int []fHueHist_Q_Bin = new int[360/BinSize];
        int index = 0;
        for (int i=0;i<=(360-BinSize);i+=BinSize)
        {   
            int temp_Q=0;//int temp_DB = 0;
            for (int j=0;j<BinSize;j++)
            {
               temp_Q += fHueHist[i+j];
               //temp_DB += fHueHist_DB[i+j];
            }
            fHueHist_Q_Bin[index] = temp_Q;
            //fHueHist_DB_Bin[index] = temp_DB;
            System.out.println("fHueHist_Q_Bin["+index+"]:"+fHueHist_Q_Bin[index]);//+" | fHueHist_DB_Bin["+index+"]:"+fHueHist_DB_Bin[index]);
            index++;
        }
        // --------------------- BINS ARE CORRECTLY OBTAINED --------------------------
        return fHueHist_Q_Bin;
    }
    
    public void Histogram_Analysis (Image imQuery, Image imDB, int BinSize, int BlockSize) throws IOException // 360 should be divisidble ny BinSize
    {        
        int[] fHueHist_Q = new int[360];
        int[] fHueHist_DB = new int[360];
        float[] hsv = new float[3];
        
        for(int i = 0; i < 352; i++)
        {
            for(int j = 0; j < 288; j++)
            {
                int iR, iG, iB;
                 
                iR = (imQuery.imgI.getRGB(i, j)>>16) & 0xFF;
                iG = (imQuery.imgI.getRGB(i, j)>>8) & 0xFF;
                iB = imQuery.imgI.getRGB(i, j) & 0xFF;                
                Color.RGBtoHSB(iR, iG, iB, hsv);                
                fHueHist_Q[(int)(hsv[0]*360)]++;
                
                // For Database Image
                iR = (imDB.imgI.getRGB(i, j)>>16) & 0xFF;
                iG = (imDB.imgI.getRGB(i, j)>>8) & 0xFF;
                iB = imDB.imgI.getRGB(i, j) & 0xFF;
                
                Color.RGBtoHSB(iR, iG, iB, hsv);                
                fHueHist_DB[(int)(hsv[0]*360)]++;
            }
        }
        // Corrctly Printing--------
        for (int i=0;i<360;i++)
        {
            System.out.print("fHueHist_Q["+i+"]:"+fHueHist_Q[i]+"  |  ");
            System.out.println("fHueHist_DB["+i+"]:"+fHueHist_DB[i]);
        }
        //---------------DIVIDE THE HISTOGRAM OF THE QUERY IMAGE INTO BINS------------*/
        int []fHueHist_Q_Bin = new int[360/BinSize];
        int []fHueHist_DB_Bin = new int[360/BinSize];
        int index = 0;
        // Divide the Histograms into bins 
        /*
        for (int i=0;i<=(360-BinSize);i+=BinSize)
        {   
            int temp_Q=0;//int temp_DB = 0;
            for (int j=0;j<BinSize;j++)
            {
               temp_Q += fHueHist_Q[i+j];
               //temp_DB += fHueHist_DB[i+j];
            }
            fHueHist_Q_Bin[index] = temp_Q;
            //fHueHist_DB_Bin[index] = temp_DB;
            System.out.println("fHueHist_Q_Bin["+index+"]:"+fHueHist_Q_Bin[index]);//+" | fHueHist_DB_Bin["+index+"]:"+fHueHist_DB_Bin[index]);
            index++;
        }
        // --------------------- BINS ARE CORRECTLY OBTAINED --------------------------
        */
        /*
        fHueHist_Q_Bin = Calculate_Histogram_Hue_Bins(imQuery, fHueHist_Q, 3);
        for (int i=0;i<120;i++)
            System.out.println("fHueHist_Q_Bin["+i+"]:"+fHueHist_Q_Bin[i]); */
            
        // ------------------- BLOCK BY BLOCK SEARCH ON THE DB IMAGE -----------------
        for(int i=0;i<=(352-BlockSize);i++)
        {
            for (int j=0; j<= (288 - BlockSize);j++)
            {
                for (int h=0; h<BlockSize; h++)
                {
                    for (int k=0; k<BlockSize ; k++)
                    {
                       
                    }
                }
            }
        }
    }
    
    public Image Recognize_Logo_using_HMap (Image imgQ, Image imgDB)
    {   
        Image OutImage = new Image(352,288);
        int[] Hue  = new int [360];
        int [][]HueMap;
        
        Hue = imgQ.GetHue(1);
        
        // et Hue Map
        HueMap = imgDB.GetHMap();
        
        for (int i=0;i<352;i++)
        {
            for (int j=0; j<288; j++)
            {
                if (HueMap[i][j] < 30 && HueMap[i][j] > 25)
                {
                    OutImage.imgI.setRGB(i,j,0x00FF0000);
                }
                else
                {
                    OutImage.imgI.setRGB(i,j,0x00000000);
                }
            }
        }     
        
        return OutImage;
    }
    
    public static int DominantHueAPX(int[] HueHist)
    {
        float  fAns = 0;
        int fTot = 0;
        int iAns;
        
        for(int i = 0;i<360;i++)
        {
            fAns += i*HueHist[i];
            fTot += HueHist[i];
        }
        
        fAns = fAns / fTot;
        
        iAns = (int) fAns;
        
        return iAns;
    }
    
    public static int[] DominantHueVec(int[] HueHist)
    {
        ArrayList<Integer> VecHue = new ArrayList<Integer>();
        
        int iCurrMax = 0, iLastMax = 0, iNext = 0, iCurr;
        int iCM = 0, iN, iC, iLM = 0;
        int iFlag = 0;
        
        for(int i = 0;i<360;i++)
        {
            iCurr = HueHist[i];
            iC = i;
            
            if(iCurrMax < iCurr && iCurr >= 2000)
            {
                iLastMax = iCurrMax;
                iCurrMax = iCurr;
                iLM = iCM;
                iCM = iC;
                iFlag = 1;
            }
            else
            {
                if(iFlag == 1)
                {
                    VecHue.add(iLM);
                    i+= 20;
                    iFlag = 0;
                }                
            }
        }               
        
        int[] ans = new int[VecHue.size()];
        for(int i = 0;i<VecHue.size();i++)
        {
            ans[i] = VecHue.get(i);
        }
        
        return ans;
    }
    
    public static int[] GetHistHueSubMatrix(int[][] matRGB, int iW, int iH)
    {
        int[] fHueHist = new int[360];        
        float[] hsv = new float[3];
        
        for(int i = 0;i<360;i++)
        {
            fHueHist[i] = 0;
        }
        
        for(int i = 0; i < iW; i++)
        {
            for(int j = 0; j < iH; j++)
            {
                int iR, iG, iB;
                                 
                        iR = (int)(matRGB[i][j] >>16) & 0xFF;
                        iG = (int)(matRGB[i][j] >>8) & 0xFF;
                        iB = (int)(matRGB[i][j] >>0) & 0xFF;
                        Color.RGBtoHSB(iR, iG, iB, hsv);
                        fHueHist[(int)(hsv[0]*360)]++;                        
            }
        }
        
        
        return fHueHist;
    }
    
    public static void Search_Candidates (Image imgQ, Image imDB, int[] iANS)
    {   
        int width = 352;
        int height = 288;
        
        int [][]matRGB = new int[44][36];
        int[] fHueHistDB = new int[360];
        int[] fHueHistQ = new int[360];
        int []Vect;
        
        fHueHistQ = imgQ.GetHue(1);        
        int DominantHueApproxDB;
        //int []DominantHueQ_Vct;
        // GET DOMINANT HUE FROM THE QUERY IMAGE
        //DominantHueQ_Vct = DominantHueVec(fHueHistQ);
        int DominantHueApproxQ = DominantHueAPX(fHueHistQ);
                
        // Single Block of 44x36
        float [] Distances = new float [(352*288)/(44*36)];
        int index = 0;
        
        for (int i=0; i<(width-44); i+=44)
        {
            for (int j=0; j<(height-36); j+=36)
            {   
                // SINGLE BLOCK SEARCH - SHIFT
                float MSE = 0.0f;
                float Euclidean_Distance = 0.0f;
                //int min = 0;
                
                for (int h=0;h<44;h++)
                {
                    for (int k=0;k<36;k++)
                    {
                        // Generate Histogram of the small block
                        matRGB[h][k] = imDB.imgI.getRGB(i+h,j+k);                                                
                    }
                }
                fHueHistDB = GetHistHueSubMatrix(matRGB,44,36);                        
                
                 // Pass fHueHist to get the Dominant Hue Component
                DominantHueApproxDB = DominantHueAPX(fHueHistDB);                                       
                // Save the DIstance Q-DB into the array
                
                // Use Vectors
                
                Distances[index] = DominantHueApproxQ - DominantHueApproxDB;
                index++;
            }
            
        }
        
        // FIND MSE
        float []SqErr = new float [Distances.length];
        // Find the Block with minimum mse
        float sum = 0;
        for(int a=0; a<(Distances.length);a++)
        {
            sum+= Distances[a];
        }
        float Mean = sum/(Distances.length);
        float []MSE = new float[Distances.length];
        for(int a=0; a<(Distances.length);a++)
        {
            MSE[a] = (Distances[a] - Mean)*((Distances[a] - Mean))/(Distances.length);
        }
        // Find Miminum 
        float min = MSE[0];
        int min_posn = 0;
        for (int a =0; a<Distances.length; a++ )
        {
            if(MSE[a]<min)
            {
                min = MSE[a]; min_posn = a;
            }
        }
        
        // Find the corresponding co-ordinates of that block
        int Min_Block_X = 44*(min_posn/8);
        int Min_Block_Y = 36*(min_posn%8);
        
        System.out.println("Matching Block : ("+Min_Block_Y+" , "+Min_Block_X+") ");
        
        iANS[0] = Min_Block_Y;
        iANS[1] = Min_Block_X;
    }
}
