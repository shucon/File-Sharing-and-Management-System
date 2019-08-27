import java.util.* ;
import java.io.* ;
import java.nio.file.Files; 
import java.nio.file.*; 
import java.net.* ;
import java.awt.* ;

public class server {
    public static Vector<Socket> ClientSockets;
    public static Vector<String> LoginNames;
    public static Vector<Chatroom> Chatrooms;
    public static DatagramSocket SocUDP;
    public static Map<String,Chatroom> ConnectedChatroom;
    public static Vector<Integer> Ports;
    public static int max_no_clients;
    server(int max_no_clients_) {
        try {
            LoginNames = new Vector<String>();
            ClientSockets = new Vector<Socket>();
            Chatrooms = new Vector<Chatroom>();
            Ports = new Vector<Integer>();
            ServerSocket Soc = new ServerSocket(6666) ;
            DatagramSocket SocUDP = new DatagramSocket(6661);
            ConnectedChatroom = new HashMap<String,Chatroom>();
            max_no_clients=max_no_clients_;
            System.out.println("Server is running on Port-6666(TCP), 6661(UDP)");
            while(1==1) {
                Socket CSoc = Soc.accept();
                AcceptClient client_ = new AcceptClient(CSoc,SocUDP) ;
            }
        }
        catch(Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }
    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.out.println("Maximum number of Users for the Server not given, taking 5 by default.");
            server server = new server(5) ;
        } else {
            server server = new server(Integer.parseInt(args[0])) ;
        }
    }
}

class AcceptClient extends Thread {
    DatagramPacket recieve_inital;
    DatagramSocket SocUDP;
    DataInputStream din;
    DataOutputStream dout;
    Socket ClientSocket;
    String LoginName;
    AcceptClient (Socket CSoc, DatagramSocket SocUDP_) throws Exception {
        ClientSocket = CSoc;
        dout = new DataOutputStream(ClientSocket.getOutputStream()) ;
        din = new DataInputStream(ClientSocket.getInputStream());
        byte[] intial = new byte[1000];
        recieve_inital = new DatagramPacket(intial, intial.length);
        SocUDP = SocUDP_;
        SocUDP.receive(recieve_inital);
        // LoginName = din.readUTF() ;
        // // if (server.LoginNames.size()==server.max_no_clients)
        // // {
        // //     System.out.println("Cannot login user: Server's maximum limit reached");
        // //     dout.writeUTF("Cannot connect: Reached Server's maximum capacity");
        // //     ClientSocket.close() ; din.close() ; dout.close() ; return;
        // // }
        // System.out.println("User "+LoginName+" logged in");
        // int port = recieve_inital.getPort();
        // server.Ports.add(port);
        // server.LoginNames.add(LoginName) ; server.ClientSockets.add(ClientSocket) ; server.ConnectedChatroom.put(LoginName,null);
        start();
    }
    public void run() {
        while(1==1) {
            try {
                String commandfromClient = new String() ;
                commandfromClient = din.readUTF() ;
                StringTokenizer tokenedcommand = new StringTokenizer(commandfromClient);
                String command=tokenedcommand.nextToken();
                if (command.equals("create_user")) {
                    LoginName = tokenedcommand.nextToken();
                    if (server.LoginNames.size()==server.max_no_clients) {
                        System.out.println("Cannot login user: Server's maximum limit reached");
                        dout.writeUTF("Cannot connect: Reached Server's maximum capacity");
                        dout.close();
                        din.close();
                        ClientSocket.close();
                        return;
                    } else {
                        File theDir = new File(LoginName);
                        if (theDir.exists() == false) {
                            theDir.mkdir();
                        }
                        System.out.println("User "+LoginName+" is created");
                        int port = recieve_inital.getPort();
                        server.ClientSockets.add(ClientSocket);
                        server.LoginNames.add(LoginName);
                        server.Ports.add(port);
                        server.ConnectedChatroom.put(LoginName, null);
                    }
                } else if (command.equals("upload")) {
                    StringTokenizer st = new StringTokenizer(commandfromClient);
                    String cmd=st.nextToken(),fl=st.nextToken();
                    String st_ = din.readUTF();
                    StringTokenizer stt = new StringTokenizer(st_);
                    stt.nextToken();
                    int fileLength = Integer.parseInt(stt.nextToken());
                    StringTokenizer fileName = new StringTokenizer(fl,"/");
                    while(fileName.hasMoreTokens()) {
                        fl=fileName.nextToken();
                    }                        
                    byte[] file_contents = new byte[1000];
                    FileOutputStream fpout = new FileOutputStream(LoginName + "/" + fl);
                    BufferedOutputStream bpout = new BufferedOutputStream(fpout);
                    int size=1000;
                    if (size > fileLength) {
                        size=fileLength;
                    }
                    while(din.read(file_contents,0,size) != -1 && fileLength != 0) {
                        bpout.write(file_contents,0,size);
                        fileLength -= size;
                        if (size > fileLength) {
                            size=fileLength;
                        }
                    }
                    System.out.println("File Recieved");
                    bpout.flush();
                } else if (command.equals("upload_udp")) {
                    StringTokenizer st = new StringTokenizer(commandfromClient);
                    String cmd = st.nextToken();
                    String fl = st.nextToken();
                    String st_ = din.readUTF();
                    StringTokenizer stt = new StringTokenizer(st_);
                    stt.nextToken();
                    int fileLength = Integer.parseInt(stt.nextToken());
                    StringTokenizer fileName = new StringTokenizer(fl, "/");
                    while(fileName.hasMoreTokens()) {
                        fl = fileName.nextToken();
                    }
                    byte[] file_contents = new byte[1000];
                    FileOutputStream fpout = new FileOutputStream(LoginName + "/" + fl);
                    BufferedOutputStream bpout = new BufferedOutputStream(fpout); 
                    DatagramPacket receivePacket;                   
                    int size = 1024;
                    file_contents = new byte[size];
                    if (size > fileLength) {
                        size=fileLength;
                    }
                    System.out.println("Length of file is: " + fileLength);
                    while(fileLength > 0) {
                        receivePacket  = new DatagramPacket(file_contents, size);
                        SocUDP.receive(receivePacket);
                        bpout.write(file_contents,0,size);
                        fileLength -= size;
                        if (size > fileLength) {
                            size=fileLength;
                        }
                    }
                    bpout.flush();
                    System.out.println("File is uploaded successfully");    
                } else if (command.equals("create_folder")) {
                    String folder = tokenedcommand.nextToken();
                    File theDir = new File(LoginName + "/" + folder);
                    if (!theDir.exists()) {
                        try {
                            theDir.mkdir();
                            System.out.println("Folder Created");
                        } catch (Exception e) {
                            System.out.println(e);
                            System.out.println("Folder Creation unsuccessful");
                        }
                    } else {
                        System.out.println("Folder Already Exists");
                    }
                } else if (command.equals("move_file")){
                    String pathsrc = LoginName + "/" + tokenedcommand.nextToken();
                    String pathdst = LoginName + "/" + tokenedcommand.nextToken();
                    try {
                        Path temp = Files.move(Paths.get(pathsrc), Paths.get(pathdst));
                        System.out.println("File moved successfully"); 
                        dout.writeUTF("File moved successfully"); 
                    } catch (Exception e) { 
                        System.out.println(e); 
                        System.out.println("Failed to move the file");
                        dout.writeUTF("Failed to move the file"); 
                    } 
                } else if (command.equals("create_group")) {
                    String chatroomName=tokenedcommand.nextToken();
                    if (server.Chatrooms.indexOf(chatroomName) == -1) {
                        Chatroom chatR = new Chatroom(chatroomName, LoginName);
                        server.Chatrooms.add(chatR);
                        dout.writeUTF("Group "+chatroomName+" created");
                    }
                } else if (command.equals("list_groups")) {
                    if (server.Chatrooms.size() == 0) {
                        dout.writeUTF("No Groups exist currently.");
                    }
                    else {
                        String outp = "";
                        for (int i = 0; i < server.Chatrooms.size(); i++) {
                            outp = outp + server.Chatrooms.elementAt(i).name + "\n";
                        }
                        dout.writeUTF(outp);
                    }                   
                } else if (command.equals("join_group")) {
                    int i;
                    String chatroomName = tokenedcommand.nextToken();
                    for (i = 0; i < server.Chatrooms.size(); i++) {
                        if (server.Chatrooms.elementAt(i).name.equals(chatroomName)) {
                            String outp=server.Chatrooms.elementAt(i).Join(LoginName);
                            dout.writeUTF(outp); 
                            server.Chatrooms.elementAt(i).Notify(LoginName+" joined the group",LoginName); 
                            break;
                        }
                    } 
                    if (i == server.Chatrooms.size()) {
                        dout.writeUTF(chatroomName+" doesn't exist");
                    }
                } else if (command.equals("leave_group")) {
                    int i;
                    String chatroomName=tokenedcommand.nextToken();
                    for (i = 0; i < server.Chatrooms.size(); i++) {
                        if (server.Chatrooms.elementAt(i).name.equals(chatroomName)) {
                            String outp=server.Chatrooms.elementAt(i).Leave(LoginName);
                            server.Chatrooms.elementAt(i).Notify(LoginName+" left the group",LoginName); 
                            if (outp.equals("DEL")) {
                                server.Chatrooms.remove(server.Chatrooms.elementAt(i));
                                dout.writeUTF("You left Group"+'\n'+chatroomName+" deleted");
                            } else {
                                dout.writeUTF(outp);
                            }
                            break;
                        }

                    } 
                    if (i == server.Chatrooms.size()) {
                        dout.writeUTF(chatroomName+" doesn't exist");
                    }         
                } else if (command.equals("share_msg")) {
                    int i = 0;
                    String msg = "";
                    while(tokenedcommand.hasMoreTokens()) {
                        msg = msg + " " + tokenedcommand.nextToken();
                    } 
                    while (i < server.Chatrooms.size() ) {
                        Chatroom C = server.Chatrooms.elementAt(i);
                        if (C.ListUsers().indexOf(LoginName) != -1) {
                            String msgfromClient = "Message From " + LoginName + "@" + C.name + ":";
                            msgfromClient += msg;
                            C.Notify(msgfromClient, LoginName);
                        }
                        i++;
                    }
                } else if (command.equals("list_detail")) {
                    int i;
                    String outp="";
                    String chatroomName=tokenedcommand.nextToken();
                    for (i=0;i<server.Chatrooms.size();i++){
                        if (server.Chatrooms.elementAt(i).name.equals(chatroomName)){
                            Chatroom C = server.Chatrooms.elementAt(i);
                            Vector<String> outpl=C.ListUsers();
                            int j = 0;
                            while (j<outpl.size()) {
                                String name = outpl.elementAt(j);
                                outp += "Username: ";
                                outp += name + "\n";
                                File folder = new File("./" + name);
                                File[] listOfFiles = folder.listFiles();
                                if (listOfFiles.length >= 1) {
                                    outp += "Files:\n";
                                }
                                int k = 0;
                                while (k < listOfFiles.length) {    
                                    if (listOfFiles[k].isFile()) {
                                        outp += listOfFiles[k].getName();
                                        outp += "\n";
                                    } else if (listOfFiles[k].isDirectory()) {
                                        String subdir = listOfFiles[k].getName();
                                        File sbfolder = new File(folder+"/"+subdir);
                                        File[] sblistOfFiles = sbfolder.listFiles();
                                        for (int p = 0; p < sblistOfFiles.length; p++) {
                                            outp += subdir + "/";
                                            outp += sblistOfFiles[p].getName() + "\n";
                                        }
                                    }
                                    k++;
                                }
                                j++;
                            }
                            break;
                        }
                    }
                    if (i==server.Chatrooms.size()) dout.writeUTF(chatroomName+" doesn't exist");
                    dout.writeUTF(outp);
                } else if(command.equals("get_file")) {
                    int i = 0;
                    String fl1 = tokenedcommand.nextToken();
                    String[] flSplit = fl1.split("/",3);
                    String groupname = flSplit[0];
                    String fileUser=flSplit[1];
                    // for(i = 0; i < server.Chatrooms.size(); i++){
                    while (i < server.Chatrooms.size()) {    
                        Chatroom C = server.Chatrooms.elementAt(i);
                        if(C.name.equals(groupname)) {
                            if(C.ListUsers().indexOf(fileUser) == -1 || C.ListUsers().indexOf(LoginName) == -1){
                                String outputError = "Either you or the user: "+fileUser+" doesn't belong to this group: " + groupname;
                                dout.writeUTF(outputError);
                                break;
                            }
                            String fl = flSplit[1] + "/" + flSplit[2];
                            File file = new File(fl);
                            long fileLength =  file.length();
                            FileInputStream fpin = new FileInputStream(file);
                            BufferedInputStream bpin = new BufferedInputStream(fpin);
                            String[] justnamear = fl.split("/",0);
                            String justname = justnamear[justnamear.length-1];
                            long current=0;
                            String outputAnswer = "FILE "+justname+" LENGTH " + fileLength;
                            dout.writeUTF(outputAnswer);
                            System.out.println(outputAnswer);
                            System.out.println(fileLength);
                            while (current != fileLength) {
                                int size;
                                if(fileLength - current >= 1000) {
                                    size = 1000;
                                    current = size + current;
                                } else {
                                    size = (int)(fileLength-current);
                                    current=fileLength;
                                }
                                byte[] file_contents = new byte[size];
                                bpin.read(file_contents, 0, size);
                                dout.write(file_contents);
                                String progress = "File sending "+(current*100/fileLength)+"% complete";
                                System.out.println(progress);
                            }
                            System.out.println("File Sent");                       
                            break;
                        }
                        i++;
                    }
                    System.out.println("Check");
                    if (i == server.Chatrooms.size()) {
                        dout.writeUTF(groupname+" doesn't exist");
                    }
                } else {
                    dout.writeUTF("Unrecognised command, please read the reference PDF file.");
                }
            } catch(Exception e) {
                e.printStackTrace(System.out);
                break;
            }
        }
    }
}

class Chatroom {
    Vector<String> Members = new Vector<String>();
    String name;
    Chatroom (String name,String member) {
        this.name = name;
        this.Members.add(member);
        server.ConnectedChatroom.put(member,this);
    }
    public String Join (String member) {
        this.Members.add(member);
        server.ConnectedChatroom.put(member,this);
        return ("Joined Chatroom "+this.name);
    }
    public String Leave (String member) {
        this.Members.remove(member);
        server.ConnectedChatroom.put(member,null);
        if (this.Members.isEmpty()) return ("DEL");
        else return("You left chatroom "+this.name);
    }
    public Vector<String> ListUsers() {
        return this.Members;
    }
    public String Add(String memberAdd) {
        if (this.Members.contains(memberAdd)) return(memberAdd+" is already a part of "+this.name);
        if (!server.LoginNames.contains(memberAdd)) return("The username "+memberAdd+" doesn't exist");
        for (int c=0; c<server.Chatrooms.size();c++)
        {
            Chatroom C = server.Chatrooms.elementAt(c);
            if (C.Members.contains(memberAdd)) return("Cannot add "+memberAdd+" to chatroom "+this.name+"\n"+memberAdd+" already a part of chatroom "+C.name);
        }
        this.Members.add(memberAdd);
        server.ConnectedChatroom.put(memberAdd,this);
        return(memberAdd+" added to chatroom "+this.name);
    }
    public void Notify(String msg,String no_notif) {
        for (int i=0;i<this.Members.size();i++)
        {
            if (!this.Members.elementAt(i).equals(no_notif))
            {
                try {
                    Socket sendSoc = server.ClientSockets.elementAt(server.LoginNames.indexOf(this.Members.elementAt(i)));
                    DataOutputStream senddout = new DataOutputStream(sendSoc.getOutputStream());
                    senddout.writeUTF(msg);
                } catch(Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
