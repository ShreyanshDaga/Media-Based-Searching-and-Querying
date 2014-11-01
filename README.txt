~*~**~**~*~**~**~*~***~****~*~*~**~~**~*~*~*~*~*~*~***~*~*~*~*~*~*~*~***~*~*
 Name: Shreyansh Dnyanesh Daga
 Name: Manan Vijay Vyas
 USCID : 6375-3348-33 	
 USCID : 7483-8632-00
 Email : sdaga@usc.edu
 Email : mvyas@usc.edu
~*~*~***~*~*~**~**~*~*~*~**~**~**~~**~*~*~~**~*~*~*~*~*~**~*~*~*~*~**~**~*~*~*~*
 ================================
 CSCI Final Project Readme File
 ================================
 Input Arguments:
 args[0] = Query Image name

 args[1] = Database Image name

 args[2] = Alpha map file name if present, otherwise skip.

 -----------------------------------------------------------------------------
 Output :
 Window 1 : Query Image
 Window 2 : Database Image
 Window 3 : Resultant Database Image with green markers for detected Object.
 -----------------------------------------------------------------------------


 Files:
 1) project_final.java : Main file of program, entry point of program.
 2) Image.java : Class file for Image and various functions.
 3) Recognizer.java : Class file for recognition, contains all tried methods for recognition.
 4) AlphaMap.java : Class file for AlphaMap to be used within Image class.
 5) KMeans.java : Class file for KMeans functionality to be used when segmentation and clustering is required.

	Image.java, AlphaMap.java and KMeans.java lie inside the package Image
	project_final.java lies inside the package called project_final - main file of program
 	Recognizer.java lies inside the package called Recognizer

 Other Dependencies:
 1) opencv-249.jar
 2) JDK 1.7 (default)

Submission Material:
- .Java file : Project written,Compiles and Executed on NetBeans
- This Readme file.