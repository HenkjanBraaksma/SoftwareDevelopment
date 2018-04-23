using UnityEngine;
using System.Collections;
using System;
using System.IO;
using System.Net.Sockets;

public class TCPConnection : MonoBehaviour
{
    public string connectionName = "Kruispuntserver";
    public string fieldInput = IP_Data.ipAddress;
    public string connectionHost = "localhost";
    public int connectionPort = 1234;

    //a true/false variable for connection status
    public bool socketReady = false;

    TcpClient socket;
    NetworkStream stream;
    StreamWriter writer;
    StreamReader reader;

    //try to initiate connection
    public void setupSocket()
    {
        try
        {
            try
            {
                string[] portBuffer = fieldInput.Split(':');
                connectionHost = portBuffer[0];
                connectionPort = Int32.Parse(portBuffer[1]);
            }
            catch(Exception e)
            {
                Debug.Log("Failed to properly parse the port");
            }
            socket = new TcpClient(connectionHost, connectionPort);
            stream = socket.GetStream();
            writer = new StreamWriter(stream);
            reader = new StreamReader(stream);
            socketReady = true;
        }
        catch (Exception e)
        {
            Debug.Log("Socket error:" + e);
        }
    }

    //send message to server
    public void writeSocket(string line)
    {
        if (!socketReady)
            return;
        String tmpString = line + "\r\n";
        writer.Write(tmpString);
        writer.Flush();
    }

    //read message from server
    public string readSocket()
    {
        String result = "";
        if (socketReady)
        {
            if (stream.DataAvailable)
            {
                Byte[] inStream = new Byte[socket.SendBufferSize];
                stream.Read(inStream, 0, inStream.Length);
                result += System.Text.Encoding.UTF8.GetString(inStream);
            }
        }
        return result;
    }

    //disconnect from the socket
    public void closeSocket()
    {
        if (!socketReady)
            return;
        writer.Close();
        reader.Close();
        socket.Close();
        socketReady = false;
    }

    //keep connection alive, reconnect if connection lost
    public void maintainConnection()
    {
        if (!stream.CanRead)
        {
            setupSocket();
        }
    }


}