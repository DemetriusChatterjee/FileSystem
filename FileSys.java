/*
 * FileSys.java
 * This program provides a cmd like prompt and creates virtual file paths with virtual files and folders
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
/*
 * Class FileSys:
 * 1. Provides a cmd type interface for the user
 * 2. Provides the cmd like promt and reads in commands using Scanner
 * 3. Calls appropriate classes to do the necessary
 */
public class FileSys {

    //outputs err if invalid command
    static void throwCommandError(){
        System.err.println("ERROR: Invalid command entered.");
        System.err.flush();
    }

    //outputs err if invalid command length
    static void throwLengthError(){
        System.err.println("ERROR: Invalid command length entered.");
        System.err.flush();
    }
    public static void main(String[] args) { 
        Scanner scan = new Scanner(System.in);
        FILESystem fileSystem = new FILESystem();
        Folder root = new Folder("/");
        FILESystem.currentDir = root;
        while(true){
            System.out.print("prompt> "); 
            String input = scan.nextLine();
            String[] command = input.split(" ");
            int size = command.length;
            if(input.equals("")){
                continue;
            }

            if(command[0].equals("exit")){
                if(size != 1){
                    throwLengthError();
                }
                break;
            }else if(command[0].equals("create")){
                
                if(size != 2){
                    throwLengthError();
                    continue;
                }
                String fileName = command[1];
                File file = new File(fileName);
                boolean addedFile = File.addPath(file);
                if(!addedFile){
                    continue;
                }
                while(scan.hasNextLine()){
                    String line = scan.nextLine();
                    if(line.length() == 0){
                        line = " ";
                    }
                    if(line.contains("~")){
                        if(line.length() > 1){
                            line = line.substring(0, line.length() - 1);
                            file.addTxt(line, true);
                        }
                        break;
                    }
                    file.addTxt(line, false);
                }
                continue;
            }else if(command[0].equals("cat")){
                if(size != 2){
                    throwLengthError();
                    continue;
                }
                File.catenate(command[1]);

            }else if(command[0].equals("rm")){
                if(size != 2){
                    throwLengthError();
                    continue;
                }
                File.removeFromPath(command[1]);

            }else if(command[0].equals("mkdir")){
                if(size != 2){
                    throwLengthError();
                    continue;
                }

                Folder folder = new Folder(command[1]);
                Folder.addPath(folder);
                
            }else if(command[0].equals("rmdir")){
                if(size != 2){
                    throwLengthError();
                    continue;
                }

                Folder.removeFromPath(command[1]);
                
            }else if(command[0].equals("cd")){
                if(size != 2){
                    throwLengthError();
                    continue;
                }
                
                Folder.cd(command[1]);

            }else if(command[0].equals("ls")){
                if(size != 1){
                    throwLengthError();
                    continue;
                }
                
                Folder.ls();

            }else if(command[0].equals("du")){
                if(size != 1){
                    throwLengthError();
                    continue;
                }

                Folder.du();

            }else if(command[0].equals("pwd")){
                if(size != 1){
                    throwLengthError();
                    continue;
                }
                
                System.out.println(Folder.pwd(FILESystem.currentDir, true));

            }else if(command[0].equals("find")){
                if(size != 2){
                    throwLengthError();
                    continue;
                }

                Folder.find(FILESystem.currentDir, command[1]);
                
            }else{
                //if the command entered is not any of the acceptable ones
                throwCommandError();
            }
        }
        scan.close();
    }
}

/*
 * Class FILESystem:
 * 1. Parent class of Folder and File class
 * 2. Stores the filePath in 2 diffent formats:
 *  a. the string one stores the path including names of files (only used for pwd)
 *  b. the arraylist only stores the folders
 */
class FILESystem {
    static Folder currentDir;   //USE currentDir for everything the String path is used, alwyas start in main by adding root Dir and setting currentDir to that
    FILESystem(){}
}

/*
 * Class Folder:
 * 1. Extends FILESystem
 * 2. Stores all the folders in the system
 */
class Folder extends FILESystem{  
    /*
     * Checking if folders already exits in the current folder OR should this be done in Path? 
     * Answer: Go to parentDIr and check if it exists in subFolders, do this for File as well
     * Q: Should this be in FILESystem then as it will be done by both?
     * Q: Should validating the folder and file name be in FileSys also as it will be done for both?
     */
    private String folderName;
    private Folder parentDirectory;
    private ArrayList<Folder> subFolders;
    private ArrayList<File> subFiles;
    
    //Folder constructor
    Folder(String name){
        this.subFiles = new ArrayList<>();
        this.folderName = name;
        this.parentDirectory = null;
        this.subFolders = new ArrayList<>();
    }

    //adds to path otherwise outputs an error message
    static void addPath(Folder addFolder){
        Folder prevFolder = FILESystem.currentDir;
        if(prevFolder.getName().equals(addFolder.getName())){
            System.err.println("ERROR: Cannot add to path. Already in " + addFolder.getName());
            System.err.flush();
            return;
        }
        for(Folder f : prevFolder.getSubFolders()){
            if(f.getName().equals(addFolder.getName())){
                System.err.println("ERROR: " + addFolder.getName() + " already exists in " + f.getName() + ".");
                System.err.flush();
                return;
            }
        }
        
        prevFolder.addSubFolder(addFolder);
        addFolder.setParentDirectory(prevFolder);
    }

    //adds a folder to the current folder
    void addSubFolder(Folder name){ 
        this.subFolders.add(name);
    }

    //adds a file to the current folder
    void addSubFile(File name){
        this.subFiles.add(name);
    }

    //checks current DIR and if present, removes it 
    static void removeFromPath(String folderToRemove){
        Folder prevFolder = FILESystem.currentDir;
        for(Folder f : prevFolder.getSubFolders()){
            if(f.getName().equals(folderToRemove)){
                prevFolder.getSubFolders().remove(f); //better to use a for loop for remove
                return;
            }
        }
        System.err.println("ERROR: Could not find " + folderToRemove + ".");
        System.err.flush();
    }

    //sets the parent directory
    void setParentDirectory(Folder prevFolder){ 
        this.parentDirectory = prevFolder;
    }

    //if folder exits in current dir then changes its name ptherwise outputs an error
    //if ".." entered, then go up a dir unless at null
    //if "/" entered then goes back to root
    //if path entered then checks if that path is valid if not then outputs an error
    static void cd(String fname){
        Folder prevFolder = FILESystem.currentDir;
        String[] fnames = fname.split("/");
        if(fnames.length <= 1){
            if(fname.equals("/")){
                while(!prevFolder.getName().equals("/")){
                    FILESystem.currentDir = prevFolder.getParentDirectory();
                    prevFolder = FILESystem.currentDir;
                }
                return;
            }else if(fname.equals("..")){
                if(prevFolder.getName().equals("/")){
                    return;
                }
                FILESystem.currentDir = prevFolder.getParentDirectory();
                return;
            }else if(prevFolder.getSubFolders().size() != 0){
                for(Folder f : prevFolder.getSubFolders()){
                    if(f.getName().equals(fname)){
                        FILESystem.currentDir = f;
                        return;
                    }
                }
            }
        }else{
            for(String part : fnames){
                if(part.equals("..")){
                    if(prevFolder.getName().equals("/")){
                        return;
                    }
                    FILESystem.currentDir = prevFolder.getParentDirectory();
                    prevFolder = FILESystem.currentDir;
                }else{
                    boolean found = false;
                    for(Folder f : prevFolder.getSubFolders()){
                        if(f.getName().equals(part)){
                            FILESystem.currentDir = f;
                            prevFolder = FILESystem.currentDir;
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        System.err.println("ERROR: Directory does not exist.");
                        System.err.flush();
                        return;
                    }
                }
            }
        }
    }

    //prints the files and folders of current dir in alphabetical order with the (*) next to all directories
    static void ls(){
        Folder prevFolder = FILESystem.currentDir;
        ArrayList<String> allFilesAndFolders = new ArrayList<>();
        for(Folder fol : prevFolder.subFolders){
            allFilesAndFolders.add(fol.getName() + " (*)");
        }
        for(File fi : prevFolder.subFiles){
            allFilesAndFolders.add(fi.getName());
        }
        Collections.sort(allFilesAndFolders);
        for(String fileOrFolder : allFilesAndFolders){
            System.out.println(fileOrFolder);
        }
    }

    //checks if any of the subfolders are of that name and if any files are that name 
    //and if they are then prints out each pwd and then calls find on each subfolder
    static void find(Folder folder, String name){
        for(File fi : folder.subFiles){
            if(fi.getName().equals(name)){
                System.out.println(pwd(fi));
            }
        }
        for(Folder fol : folder.subFolders){
            if(fol.getName().equals(name)){
                System.out.println(pwd(fol, false));
            }
            find(fol, name);
        }
    }

    //prints all the subdirectories and directories and the total characters at the end
    static void du(){
        Folder prevFolder = FILESystem.currentDir;
        long total = du(prevFolder);
        System.out.println(total);
    }

    //recursive helper method for the du above 
    static long du(Folder folder){
        long total = 0;
        for(File f : folder.getSubFiles()){
            total += f.getFileSpace();
        }
        for(Folder fo : folder.getSubFolders()){
            total += du(fo);
        }
        return total;
    }

    //returns the pwd of the folder
    static String pwd(Folder name, boolean isFirstCall){
        if(name.getParentDirectory() == null){
            if(isFirstCall && name.getName().equals("/")){
                return "/";
            }
            return "";
        }
        return pwd(name.getParentDirectory(), false) + "/" + name.getName();
    }

    //returns the pwd of the file
    static String pwd(File fname){
        return pwd(fname.getParentDirectory(), false) + fname.getName();
    }

    //returns the current parent directory of the folder
    Folder getParentDirectory(){ 
        return this.parentDirectory;
    }

    //returns the subfolders under the folder
    ArrayList<Folder> getSubFolders(){ 
        return this.subFolders;
    }

    //implements the abstract method of getting all subfiles under this dir
    ArrayList<File> getSubFiles(){ 
        return this.subFiles;
    }

    //returns the folderName associated with that Folder
    String getName(){ 
        return this.folderName;
    }
}

/*
 * Class File:
 * 1. Extends FILESystem
 * 2. Stores the files in the system
 */
class File extends FILESystem{
    private String fileName;
    private ArrayList<String> fileContent;
    private int fileSpace;
    private Folder parentDirectory;

    //constructor for File
    File(String name){
        this.fileName = name;
        this.fileSpace = 0;
        this.fileContent = new ArrayList<>();
        this.parentDirectory = null;
    }

    //adds File to the system and returns true otherwise outputs an error
    static boolean addPath(File addFile){
        Folder prevFolder = FILESystem.currentDir;
        if(prevFolder.getName().equals(addFile.getName())){
            System.err.println("ERROR: File name already exists in current directory as a folder.");
            System.err.flush();
            return false;
        }
        for(File fi : prevFolder.getSubFiles()){
            if(fi.getName().equals(addFile.getName())){
                System.err.println("ERROR: File already exists in current directory.");
                System.err.flush();
                return false;
            }
        }
        addFile.setParentDirectory(prevFolder);
        prevFolder.addSubFile(addFile);
        return true;
    }

    //checks current dir and sees if such a file exits, if it does then it removes otherwise outputs an error
    static void removeFromPath(String removeFile){
        Folder prevFolder = FILESystem.currentDir;
        for(File f : prevFolder.getSubFiles()){
            if(f.getName().equals(removeFile)){
                prevFolder.getSubFiles().remove(f); //better to use a for loop for remove 
                return;
            }
        }
        System.err.println("ERROR: Could not find " + removeFile + ".");
        System.err.flush();
    }

    //if file is present in current dir then prints it out otherwise outputs an error
    static void catenate(String fileName){
        Folder prevFolder = FILESystem.currentDir;
        for(File f : prevFolder.getSubFiles()){
            if(f.getName().equals(fileName)){
                f.readTxt();
                return;
            }
        }
        System.err.println("ERROR: File does not exist in current directory.");
        System.err.flush();
    }

    //returns the pwd of the folder
    String pwd(Folder name){
        if(name.getParentDirectory().getName().equals(null)){
            return "";
        }
        return pwd(name.getParentDirectory()) + "/" + name.getParentDirectory().getName();
    }

    //returns the pwd of the file
    String pwd(File fname){
        return pwd(fname.getParentDirectory()) + fname.getName();
    }

    //sets the parentDirectory of the file
    void setParentDirectory(Folder prevFolder) {
        this.parentDirectory = prevFolder;
    }

    //adds fileContent to txt file by taking in a new line and adding it to an arraylist
    void addTxt(String newWords, boolean isLastLine){
        if(newWords.length() < 1){
            return;
        }
        String currLine = "";
        for(int i = 0; i < newWords.length(); i++){
            currLine += newWords.charAt(i);
            this.increaseFileSpace();
        }
        this.fileContent.add(currLine);
        if (!isLastLine) {
            this.fileContent.add("/n"); //USE THIS AS INDICATOR WHEN PRINING OUT CONTENT (indicator for new line)
        }
    }

    //prints all the text out onto the screen line by line
    void readTxt(){
        for(String s : this.getFileContent()){
            if(s.equals("/n")){
                System.out.print("\n");
            }else{
                System.out.print(s);
            }
        }
        System.out.println();
    }

    //increments file space by 1 (used in addTxt so after every char added, this is called)
    void increaseFileSpace(){
        this.fileSpace++;
    }

    //returns the fileContent of the file
    ArrayList<String> getFileContent(){
        return this.fileContent;
    }

    //returns the fileSpace which will be used in
    int getFileSpace(){  
        return this.fileSpace;
    }

    //returns the folderName associated with that File
    Folder getParentDirectory(){ 
        return this.parentDirectory;
    }

    //returns the fileName associated with that File
    String getName(){ 
        return this.fileName;
    }
}