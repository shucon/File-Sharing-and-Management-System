import java.awt.* ;
import java.io.* ;
import java.net.* ;
import java.util.* ;

public class client {
    public static String ip = "127.0.0.1";
    public static int port = 6661;
    public static DatagramSocket clientSocUDP;
    public static void main(String args[]) {
        try {
            clientSocUDP = new DatagramSocket();
            Socket clientSoc;
            clientSoc = new Socket(ip,6666) ;
            System.out.println("Connected to Server at localhost Port-6666(TCP)");
            DataInputStream din;
            din = new DataInputStream(clientSoc.getInputStream());
            DataOutputStream dout;
            dout = new DataOutputStream(clientSoc.getOutputStream());
            String a = "hrlli";
            byte[] file_contents = new byte[1000];
            file_contents = a.getBytes();
            DatagramPacket initial = new DatagramPacket(file_contents,file_contents.length,InetAddress.getByName(ip),port);
            clientSocUDP.send(initial);
            String LoginName;
            // if (args.length==0)
            // {
            //     System.out.println("No Username given\n"); System.exit(0);
            // }
            // LoginName=args[0];
            // dout.writeUTF(LoginName);

            //Recieve messages
            // new Thread(new RecievedMessagesHandler (din,LoginName)).start();

            //Send messages
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String inputLine = null;
            while(1==1) {
                try {
                    inputLine = bufferedReader.readLine();
                    dout.writeUTF(inputLine);
                    if (inputLine.equals("LOGOUT")) {
                        clientSoc.close(); din.close(); dout.close();
                        System.out.println("Logged Out");
                        System.exit(0);
                    }
                    StringTokenizer tokenedcommand = new StringTokenizer(inputLine);
                    //check file transfer
                    String comm;
                    String fl;
                    String typ;
                    comm = tokenedcommand.nextToken();
                    if (comm.equals("create_user")) {
                        if (tokenedcommand.hasMoreTokens()) {
                            fl = tokenedcommand.nextToken();
                            LoginName = fl;
                            new Thread(new RecievedMessagesHandler (din,LoginName)).start();

                        } else {
                            System.out.println("Error! No Username given");
                        }
                    } else if (comm.equals("upload")) {
                        fl = tokenedcommand.nextToken();
                        System.out.println(fl);
                        File file = new File(fl);
                        FileInputStream fpin = new FileInputStream(file);
                        BufferedInputStream bpin = new BufferedInputStream(fpin);
                        long fileLength =  file.length();
                        long current=0;
                        dout.writeUTF("LENGTH "+fileLength);
                        try {
                            while(current != fileLength) {
                                int size=1000;
                                if (fileLength - current >= size) {
                                    current = size + current;
                                } else {
                                    size = (int)(fileLength-current);
                                    current = fileLength;
                                }
                                file_contents = new byte[size];
                                bpin.read(file_contents, 0, size);
                                dout.write(file_contents);
                                System.out.println("Sending file ..."+(current*100/fileLength)+"% complete");
                            }
                            System.out.println("File Sent successfully");                       
                        } catch (FileSendingException e) {
                            System.out.println(e);                       
                            System.out.println("File sending unsuccessfull");                       
                        }
                    } else if (comm.equals("upload_udp")) {
                        int size = 1024;
                        fl = tokenedcommand.nextToken();
                        File file = new File(fl);
                        FileInputStream fpin = new FileInputStream(file);
                        BufferedInputStream bpin = new BufferedInputStream(fpin);
                        long current = 0;
                        long fileLength = file.length();
                        long start =System.nanoTime();
                        dout.writeUTF("LENGTH " + fileLength);
                        try {
                            while(current != fileLength) {
                                if (fileLength - current >= size) {
                                    current = size + current;
                                } else {
                                    size = (int)(fileLength-current);
                                    current = fileLength;
                                }
                                file_contents = new byte[size];
                                bpin.read(file_contents, 0, size);
                                DatagramPacket sendPacket = new DatagramPacket(file_contents, size, InetAddress.getByName(ip), port);
                                clientSocUDP.send(sendPacket);
                                System.out.println("File sending "+(current*100/fileLength)+"% complete");
                            }
                            System.out.println("File Sent");
                        } catch (FileSendingException e) {
                            System.out.println(e);                       
                            System.out.println("File sending unsuccessfull");                       
                        }
                    }
                    // else if (comm.equals("reply"))
                    // {
                    //     boolean isFile=false;
                    //     if (tokenedcommand.hasMoreTokens())
                    //     {
                    //         fl=tokenedcommand.nextToken();
                    //         if (tokenedcommand.hasMoreTokens())
                    //         {
                    //             typ=tokenedcommand.nextToken();
                    //             //file transfer
                    //             if (typ.equals("tcp"))
                    //             {
                    //                 isFile=true;
                    //                 File file = new File(fl);
                    //                 FileInputStream fpin = new FileInputStream(file);
                    //                 BufferedInputStream bpin = new BufferedInputStream(fpin);
                    //                 long fileLength =  file.length(), current=0, start = System.nanoTime();
                    //                 dout.writeUTF("LENGTH "+fileLength);
                    //                 while(current!=fileLength)
                    //                 {
                    //                     int size=1000;
                    //                     if (fileLength - current >= size) current+=size;
                    //                     else {
                    //                         size = (int)(fileLength-current);
                    //                         current=fileLength;
                    //                     }
                    //                     file_contents = new byte[size];
                    //                     bpin.read(file_contents,0,size); dout.write(file_contents);
                    //                     System.out.println("Sending file ..."+(current*100/fileLength)+"% complete");
                    //                 }
                    //                 System.out.println("File Sent");
                    //             }
                    //             else if (typ.equals("udp"))
                    //             {
                    //                 int size=1024;
                    //                 isFile=true;
                    //                 File file = new File(fl);
                    //                 FileInputStream fpin = new FileInputStream(file);
                    //                 BufferedInputStream bpin = new BufferedInputStream(fpin);
                    //                 long fileLength = file.length(), current =0, start =System.nanoTime();
                    //                 dout.writeUTF("LENGTH "+fileLength);
                    //                 while(current!=fileLength)
                    //                 {
                    //                     if (fileLength - current >= size) current+=size;
                    //                     else {
                    //                         size = (int)(fileLength-current);
                    //                         current=fileLength;
                    //                     }
                    //                     file_contents = new byte[size];
                    //                     bpin.read(file_contents,0,size);
                    //                     DatagramPacket sendPacket = new DatagramPacket(file_contents,size,InetAddress.getByName(ip),port);
                    //                     clientSocUDP.send(sendPacket);
                    //                     System.out.println("Sending file ..."+(current*100/fileLength)+"% complete");
                    //                 }
                    //                 System.out.println("File Sent");
                    //             }
                    //         }
                    //     }
                    // }
                } catch (Exception e) { 
                    System.out.println(e);
                    break;
                }
            }
        }
        //catch(UnknownHostException e) {System.out.println("Cannot find Server"); System.exit(0);}
        //catch(IOException e) {System.out.println(e);System.exit(0);}
        catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}

class RecievedMessagesHandler implements Runnable {
    private DataInputStream server; private String LoginName;
    public RecievedMessagesHandler(DataInputStream server,String LoginName) {
        this.server = server; this.LoginName = LoginName;
    }
    @Override
    public void run() {
        String inputLine=null;
        while(1==1) {
            try {
                inputLine=server.readUTF();
                StringTokenizer st = new StringTokenizer(inputLine);
                if (st.nextToken().equals("FILE")) {
                    //File recienve
                    String fileName=st.nextToken();
                    String typ=st.nextToken();
                    st.nextToken();
                    int fileLength = Integer.parseInt(st.nextToken());
                    System.out.println("Recieving file " + fileName);
                    byte[] file_contents = new byte[1000];
                    FileOutputStream fpout = new FileOutputStream(LoginName + "/" + fileName);
                    BufferedOutputStream bpout = new BufferedOutputStream(fpout);
                    DatagramPacket receivePacket;
                    if (typ.equals("TCP")) {
                        int size=1000;
                        if (size>fileLength)size=fileLength;
                        while((bytesRead=server.read(file_contents,0,size))!=-1 && fileLength>0) {
                            bpout.write(file_contents,0,size);
                            fileLength-=size; if (size>fileLength)size=fileLength;
                        }
                        bpout.flush();
                        System.out.println("File Recieved");
                    } else {
                        int size = 1024;
                        file_contents = new byte[size];
                        if (size>fileLength) size=fileLength;
                        System.out.println(fileLength);
                        while(fileLength>0)
                        {
                            receivePacket  = new DatagramPacket(file_contents, size);
                            System.out.println("s");
                            client.clientSocUDP.receive(receivePacket);
                            System.out.println("r");
                            bpout.write(file_contents,0,size);
                            System.out.println("w");
                            fileLength-=size; if (size>fileLength)size=fileLength;
                        }
                        bpout.flush();
                        System.out.println("File Recieved");
                    }
                }
                else
                    System.out.println(inputLine);
            }
            catch(Exception e) {e.printStackTrace(System.out); break;}
        }
    }
}
