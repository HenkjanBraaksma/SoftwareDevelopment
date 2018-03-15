using UnityEngine;
using System.Collections;
using System;
using System.IO;
using System.Net.Sockets;

public class TCPConnection : MonoBehaviour
{
    public string connectionName = "Localhost";
    public string connectionHost = "127.0.0.1";
    public int connectionPort = 27015;

    //a true/false variable for connection status
    public bool socketReady = false;

    TcpClient Socket;
    NetworkStream Stream;
    StreamWriter Writer;
    StreamReader Reader;

    //try to initiate connection
    public void setupSocket()
    {
        try
        {
            Socket = new TcpClient(connectionHost, connectionPort);
            Stream = Socket.GetStream();
            Writer = new StreamWriter(Stream);
            Reader = new StreamReader(Stream);
            socketReady = true;
        }
        catch (Exception e)
        {
            Debug.Log("Socket error:" + e);
        }
    }

    //send message to server
    public void writeSocket(string theLine)
    {
        if (!socketReady)
            return;
        String tmpString = theLine + "\r\n";
        Writer.Write(tmpString);
        Writer.Flush();
    }

    //read message from server
    public string readSocket()
    {
        String result = "";
        if (Stream.DataAvailable)
        {
            Byte[] inStream = new Byte[Socket.SendBufferSize];
            Stream.Read(inStream, 0, inStream.Length);
            result += System.Text.Encoding.UTF8.GetString(inStream);
        }
        return result;
    }

    //disconnect from the socket
    public void closeSocket()
    {
        if (!socketReady)
            return;
        Writer.Close();
        Reader.Close();
        Socket.Close();
        socketReady = false;
    }

    //keep connection alive, reconnect if connection lost
    public void maintainConnection()
    {
        if (!Stream.CanRead)
        {
            setupSocket();
        }
    }


}