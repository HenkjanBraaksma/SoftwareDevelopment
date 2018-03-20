using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TrafficLightSignal
{
    float id;
    string lightStatus;
}

public class ReceiveSignal
{
    string type;
    bool bridgeOpen;
    List<TrafficLightSignal> trafficLights;
    bool status;
}