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
                        } catch (CreateFolderException e) {
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
                    } catch (FileMoveException e) { 
                        System.out.println(e); 
                        System.out.println("Failed to move the file");
                        dout.writeUTF("Failed to move the file"); 
                    } 
                // } else if (command.equals("LOGOUT")) {
                //     Chatroom C=server.ConnectedChatroom.get(LoginName);
                //     if (C != null) {
                //         String outp=server.ConnectedChatroom.get(LoginName).Leave(LoginName);
                //         if (outp.equals("DEL")) {
                //             server.Chatrooms.remove(C);
                //         } else {
                //             dout.writeUTF(outp);
                //         }
                //         din.close();
                //         dout.close();
                //         ClientSocket.close();
                //         if (server.Chatrooms.contains(C)) {
                //             C.Notify(LoginName+" left the chatroom",LoginName);
                //         }
                //         C = null;
                //     }
                //     server.LoginNames.remove(LoginName);
                //     server.ClientSockets.remove(ClientSocket) ;
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
                // } else if (command.equals("share_msg_in_group")) {
                //     String chatroomName=tokenedcommand.nextToken();
                //     String msgfromClient=LoginName+"@"+chatroomName+":";
                //     int i=0;
                //     for (i=0;i<server.Chatrooms.size();i++){
                //         if (server.Chatrooms.elementAt(i).name.equals(chatroomName)){
                //             Chatroom C = server.Chatrooms.elementAt(i);
                //             if (C.ListUsers().indexOf(LoginName)==-1){
                //                 dout.writeUTF("You are not part of this group");
                //                 break;
                //             }
                //             while(tokenedcommand.hasMoreTokens()) 
                //                 msgfromClient=msgfromClient+" "+tokenedcommand.nextToken();
                //             C.Notify(msgfromClient,LoginName);
                //             break;
                //         }
                //     }
                //     if (i == server.Chatrooms.size()) {
                //         dout.writeUTF(chatroomName+" doesn't exist");
                //     }
                } else if (command.equals("share_msg")) {
                    int i;
                    String msg="";
                    while(tokenedcommand.hasMoreTokens()) {
                        msg = msg + " " + tokenedcommand.nextToken();
                    } 
                    for (i = 0; i < server.Chatrooms.size(); i++) {
                        Chatroom C = server.Chatrooms.elementAt(i);
                        if (C.ListUsers().indexOf(LoginName) != -1) {
                            String msgfromClient = LoginName + "@" + C.name + ":";
                            msgfromClient += msg;
                            C.Notify(msgfromClient, LoginName);
                        }
                    }
                } else if (command.equals("list_detail")) {
                    String chatroomName=tokenedcommand.nextToken();
                    String outp="";
                    int i=0;
                    for (i=0;i<server.Chatrooms.size();i++){
                        if (server.Chatrooms.elementAt(i).name.equals(chatroomName)){
                            Chatroom C = server.Chatrooms.elementAt(i);
                            Vector<String> outpl=C.ListUsers();
                            String out="";
                            for (int j=0;j<outpl.size();j++){
                                String name=outpl.elementAt(j);
                                outp+= "User: "+name+"\n";
                                File folder = new File("./"+name);
                                File[] listOfFiles = folder.listFiles();
                                if (listOfFiles.length!=0)
                                    outp+="Files:\n";
                                for (int k = 0; k < listOfFiles.length; k++) {
                                    if (listOfFiles[k].isFile()) 
                                    {
                                        outp+=listOfFiles[k].getName() + "\n";
                                    }
                                    else if (listOfFiles[k].isDirectory())
                                    {
                                        String subdir = listOfFiles[k].getName();
                                        File sbfolder = new File(folder+"/"+subdir);
                                        File[] sblistOfFiles = sbfolder.listFiles();
                                        for (int p=0;p<sblistOfFiles.length;p++)
                                            outp+=subdir+"/"+sblistOfFiles[p].getName()+"\n";
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (i==server.Chatrooms.size()) dout.writeUTF(chatroomName+" doesn't exist");
                    dout.writeUTF(outp);
                }
                // else if (command.equals("list"))
                // {
                //     String nxtcomm=tokenedcommand.nextToken();
                //     if (nxtcomm.equals("chatrooms"))
                //     {
                //         String outp="";
                //         if (server.Chatrooms.size()==0) dout.writeUTF("No Chatrooms exist");
                //         else
                //         {
                //             for (int i=0;i<server.Chatrooms.size();i++) outp=outp+server.Chatrooms.elementAt(i).name+"\n";
                //             dout.writeUTF(outp);
                //         }
                //     }
                //     else if (nxtcomm.equals("users"))
                //     {
                //         if (server.ConnectedChatroom.get(LoginName)==null) dout.writeUTF("You are not part of any chatroom");
                //         else
                //         {
                //             Vector<String> outpl=server.ConnectedChatroom.get(LoginName).ListUsers();
                //             String outp="";
                //             for (int i=0;i<outpl.size();i++)outp=outp+outpl.elementAt(i)+"\n";
                //             dout.writeUTF(outp);
                //         }
                //     }
                //     else {dout.writeUTF("Unrecognised Command");}
                // }
                // else if (command.equals("join"))
                // {
                //     String chatroomName=tokenedcommand.nextToken();
                //     if (server.ConnectedChatroom.get(LoginName)!=null) dout.writeUTF("You are already part of chatroom "+server.ConnectedChatroom.get(LoginName));
                //     else
                //     {
                //         int i=0;
                //         for (i=0;i<server.Chatrooms.size();i++) if (server.Chatrooms.elementAt(i).name.equals(chatroomName))
                //         {
                //             String outp=server.Chatrooms.elementAt(i).Join(LoginName);
                //             dout.writeUTF(outp); server.Chatrooms.elementAt(i).Notify(LoginName+" joined the chatroom",LoginName); break;
                //         }
                //         if (i==server.Chatrooms.size()) dout.writeUTF(chatroomName+" doesn't exist");
                //     }
                // }
                // else if (command.equals("leave"))
                // {
                //     if (server.ConnectedChatroom.get(LoginName)==null) dout.writeUTF("You are not part of any chatroom");
                //     else
                //     {
                //         Chatroom c = server.ConnectedChatroom.get(LoginName);
                //         String name_=c.name;
                //         String outp = server.ConnectedChatroom.get(LoginName).Leave(LoginName);
                //         c.Notify(LoginName+" left the chatroom",LoginName);
                //         if (outp.equals("DEL"))
                //         {
                //             server.Chatrooms.remove(c); c=null;
                //             dout.writeUTF("You left Chatroom "+name_+'\n'+name_+" deleted");
                //         }
                //         else dout.writeUTF(outp);
                //     }
                // }
                // else if (command.equals("add"))
                // {
                //     String user = tokenedcommand.nextToken();
                //     if (server.ConnectedChatroom.get(LoginName)==null) dout.writeUTF("You are not a part of any chatroom");
                //     else
                //     {
                //         String outp = server.ConnectedChatroom.get(LoginName).Add(user);
                //         if (!outp.contains("Connot"))
                //             server.ConnectedChatroom.get(LoginName).Notify(LoginName+" added "+user+" to chatroom "+server.ConnectedChatroom.get(LoginName).name,LoginName);
                //         dout.writeUTF(outp);
                //     }
                // }
                // else if (command.equals("reply"))
                // {
                //     StringTokenizer st = new StringTokenizer(commandfromClient);
                //     String cmd=st.nextToken(),fl,tp;
                //     boolean isFile=false;
                //     if (st.hasMoreTokens())
                //     {
                //         fl=st.nextToken();
                //         if (st.hasMoreTokens())
                //         {
                //             tp=st.nextToken();
                //             if (tp.equals("tcp"))
                //             {
                //                 isFile=true;
                //                 //File transfer
                //                 Chatroom C = server.ConnectedChatroom.get(LoginName);
                //                 if (C==null) dout.writeUTF("You are not part of any chatroom");
                //                 else
                //                 {
                //                     String st_ = din.readUTF(); StringTokenizer stt = new StringTokenizer(st_); stt.nextToken() ; int fileLength = Integer.parseInt(stt.nextToken());
                //                     StringTokenizer fileName = new StringTokenizer(fl,"/"); while(fileName.hasMoreTokens())fl=fileName.nextToken();
                //                     C.Notify("FILE "+fl+" TCP  LENGTH "+fileLength,LoginName);
                //                     byte[] file_contents = new byte[1000];
                //                     int bytesRead=0,size=1000;
                //                     if (size>fileLength)size=fileLength;
                //                     while((bytesRead=din.read(file_contents,0,size))!=-1 && fileLength>0)
                //                     {
                //                         for (int i=0;i<C.Members.size();i++)
                //                         {
                //                             if (!C.Members.elementAt(i).equals(LoginName))
                //                             {
                //                                 DataOutputStream senddout = new DataOutputStream(server.ClientSockets.elementAt(server.LoginNames.indexOf(C.Members.elementAt(i))).getOutputStream());
                //                                 senddout.write(file_contents,0,size);
                //                             }
                //                         }
                //                         fileLength-=size; if (size>fileLength) size=fileLength;
                //                     }
                //                     System.out.println("Sent");
                //                 }
                //             }
                //             else if (tp.equals("udp"))
                //             {
                //                 isFile=true;
                //                 //File transfer
                //                 Chatroom C = server.ConnectedChatroom.get(LoginName);
                //                 if (C==null) dout.writeUTF("You are not part of any chatroom");
                //                 else
                //                 {
                //                     String st_ = din.readUTF(); StringTokenizer stt = new StringTokenizer(st_); stt.nextToken() ; int fileLength = Integer.parseInt(stt.nextToken());
                //                     StringTokenizer fileName = new StringTokenizer(fl,"/"); while(fileName.hasMoreTokens())fl=fileName.nextToken();
                //                     C.Notify("FILE "+fl+" UDP LENGTH "+fileLength,LoginName);
                //                     int size = 1024;
                //                     byte[] file_contents = new byte[size];
                //                     if (size>fileLength)size=fileLength;
                //                     //System.out.println(fileLength);
                //                     DatagramPacket packetUDP;
                //                     while(fileLength>0)
                //                     {
                //                         packetUDP = new DatagramPacket(file_contents,size);
                //                         SocUDP.receive(packetUDP);
                //                         for (int i=0;i<C.Members.size();i++)
                //                         {
                //                             if (!C.Members.elementAt(i).equals(LoginName))
                //                             {
                //                                 packetUDP = new DatagramPacket(file_contents,size,InetAddress.getByName("127.0.0.1"),Integer.parseInt((server.Ports.elementAt(server.LoginNames.indexOf(C.Members.elementAt(i)))).toString()));
                //                                 SocUDP.send(packetUDP);
                //                             }
                //                         }
                //                         fileLength-=size; if (size>fileLength) size=fileLength;
                //                     }
                //                 }
                //             }
                //         }
                //     }
                //     if (isFile==false)
                //     {
                //         String msgfromClient=LoginName+":";
                //         Chatroom C = server.ConnectedChatroom.get(LoginName);
                //         while(tokenedcommand.hasMoreTokens()) msgfromClient=msgfromClient+" "+tokenedcommand.nextToken();
                //         if (C==null) dout.writeUTF("You are not part of any chatroom");
                //         else C.Notify(msgfromClient,LoginName);
                //     }
                // }
                else
                {
                    dout.writeUTF("Unrecognised command");
                }
            }
            catch(Exception e) {
                e.printStackTrace(System.out) ; break;
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
                }
                catch(Exception e){ int ii=0;  }
            }
        }
    }
}
