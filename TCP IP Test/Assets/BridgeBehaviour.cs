using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BridgeBehaviour : MonoBehaviour {

    public bool signalChange = false;
    public float rotationSpeed = 45;

    private float targetZRotation;
    private float resultRotation;
    private bool open = false;
    private bool busy = false;
    private TrafficLightBehaviour[] trafficLights;
    private Transform bridgeShape;

    private LightsController controller;

    // Use this for initialization
    void Start () {
        controller = this.transform.root.GetComponent<LightsController>();
        trafficLights = GetComponentsInChildren<TrafficLightBehaviour>();
        bridgeShape = transform.GetChild(0);
	}
	
	// Update is called once per frame
	void Update () {
		if(signalChange && !busy)
        {
            SetNewRotation();
        }
        if(busy)
        {
            OpenCloseBridge();
        }
	}

    void OpenCloseBridge()
    {
        float currentRotation = bridgeShape.rotation.eulerAngles.z;
        float goalRotation;

        if (open) goalRotation = 0; else goalRotation = 90;

        if (targetZRotation < -1 || targetZRotation > 1)
        {
            if (0 < targetZRotation)
            {
                currentRotation -= rotationSpeed * Time.deltaTime;
                bridgeShape.rotation = Quaternion.Euler(0, 25, currentRotation);
                targetZRotation -= rotationSpeed * Time.deltaTime;
            }
            if (0 > targetZRotation)
            {
                currentRotation += rotationSpeed * Time.deltaTime;
                bridgeShape.rotation = Quaternion.Euler(0, 25, currentRotation);
                targetZRotation += rotationSpeed * Time.deltaTime;
            }
        }
        else
        {
            bridgeShape.rotation = Quaternion.Euler(0, 25, goalRotation);
            signalChange = false;
            busy = false;
            open = !open;

            if(open)
            {
                foreach(TrafficLightBehaviour t in trafficLights)
                {
                    if (t.lightID != "1.13")
                        t.ChangeLight("green");
                }
            }
            else
            {
                foreach (TrafficLightBehaviour t in trafficLights)
                {
                    if (t.lightID == "1.13")
                        t.ChangeLight("green");
                }
            }
            controller.BridgeSignal(open);
        }
    }

    public void SetNewRotation()
    {
        float newAngle;
        if (open) newAngle = 90; else newAngle = -90;
        targetZRotation = newAngle;
        resultRotation = transform.rotation.eulerAngles.z + newAngle;
        foreach (TrafficLightBehaviour t in trafficLights) t.ChangeLight("red");
        busy = true;
    }
}
