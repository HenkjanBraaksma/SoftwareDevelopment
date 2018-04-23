using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LightsController : MonoBehaviour {

    TCPConnection tcp;
    TrafficLightBehaviour[] trafficLightChildren;
    BridgeBehaviour bridgeChild;

    private void Awake()
    {
        tcp = gameObject.AddComponent<TCPConnection>();
    }

    void Start () {
        if(IP_Data.connectToTCP)
            tcp.setupSocket();
        trafficLightChildren = GetComponentsInChildren<TrafficLightBehaviour>();
        bridgeChild = GetComponentInChildren<BridgeBehaviour>();
	}
	
	// Update is called once per frame
	void Update () {
        ReadFromSocket();
	}

    public void TriggerSignal(string lightID, int triggerID, bool triggerBool)
    {
        Debug.Log("A trigger has been called on Stoplight " + lightID + " Trigger " + triggerID);
        string type;
        if (triggerID == 1)
            type = "PrimaryTrigger";
        else
            type = "SecondaryTrigger";

        LightSignal newSignal = new LightSignal(type, lightID, triggerBool);
        string newSignalJSON = JsonUtility.ToJson(newSignal);

        SendToServer(newSignalJSON);
    }

    public void BridgeSignal(bool bridgeStatus)
    {
        Debug.Log("The bridge has just finished opening/closing");
        BridgeSignal newSignal = new BridgeSignal("BridgeData", bridgeStatus);
        string newSignalJSON = JsonUtility.ToJson(newSignal);

        SendToServer(newSignalJSON);
    }

    //Reads a JSON message from the socket, if any have appeared, and processes it.
    void ReadFromSocket()
    {
        string serverSays = tcp.readSocket();
        if (serverSays != "")
        {
            Debug.Log("SERVER MESSGAGE JSON: " + serverSays);

            string[] messages = serverSays.Split('\n');

            foreach(string message in messages)
            {
                if(message[0] == '{')
                {
                    ReceiveSignal received = JsonUtility.FromJson<ReceiveSignal>(message);
                    ProcessSignal(received);
                    Debug.Log("[SERVER]" + serverSays);
                }
            }
        }
    }

    //Sends a message through the socket.
    public void SendToServer(string str)
    {
        if(tcp.socketReady)
        {
            tcp.writeSocket(str);
            Debug.Log("[CLIENT] -> " + str);
        }
        else
        {
            Debug.Log("Socket not ready!");
        }
    }

    //Interprets the signal: Always does lights if there are any, and otherwise, runs a different job depending on the type.
    public void ProcessSignal(ReceiveSignal signal)
    {
        TrafficLightBehaviour[] lightsBuffer = trafficLightChildren;
        foreach(TrafficLightData lightSignal in signal.trafficLights)
        {
            foreach(TrafficLightBehaviour light in lightsBuffer)
            {
                if(light.lightID == lightSignal.id)
                {
                    light.ChangeLight(lightSignal.lightStatus);
                }
            }
        }
        if (signal.type == "BridgeData")
        {
            bridgeChild.signalChange = true;
        }
    }
}
