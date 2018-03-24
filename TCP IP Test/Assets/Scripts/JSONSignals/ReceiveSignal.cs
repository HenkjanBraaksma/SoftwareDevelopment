using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

[Serializable]
public class ReceiveSignal
{
    public string type;
    public bool bridgeOpen;
    public List<TrafficLightData> trafficLights;
    public bool status;
}

[Serializable]
public class TrafficLightData
{
    public string id;
    public string lightStatus;
}