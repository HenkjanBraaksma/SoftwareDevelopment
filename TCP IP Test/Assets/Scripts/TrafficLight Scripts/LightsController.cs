using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LightsController : MonoBehaviour {

    TCPConnection tcp;
    TrafficLightBehaviour[] trafficLightChildren;

    private void Awake()
    {
        tcp = gameObject.AddComponent<TCPConnection>();
    }

    void Start () {
        tcp.setupSocket();
        trafficLightChildren = GetComponentsInChildren<TrafficLightBehaviour>();
	}
	
	// Update is called once per frame
	void Update () {
        ReadFromSocket();
	}

    public void TriggerSignal(string lightID, int triggerID)
    {
        Debug.Log("A trigger has been called on Stoplight " + lightID + " Trigger " + triggerID);
        string type;
        if (triggerID == 1)
            type = "PrimaryTrigger";
        else
            type = "SecondaryTrigger";

        SendSignal newSignal = new SendSignal(type, lightID, true);
        string newSignalJSON = JsonUtility.ToJson(newSignal);

        SendToServer(newSignalJSON);
    }

    void ReadFromSocket()
    {
        string serverSays = tcp.readSocket();
        if (serverSays != "")
        {
            ReceiveSignal received = JsonUtility.FromJson<ReceiveSignal>(serverSays);
            ProcessSignal(received);
            Debug.Log("[SERVER]" + serverSays);
        }
    }

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
    }
}
