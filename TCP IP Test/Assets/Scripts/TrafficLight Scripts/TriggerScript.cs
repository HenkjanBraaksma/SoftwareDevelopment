﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TriggerScript : MonoBehaviour {

    public int triggerID = 1;

    private LightsController controller;
    private string lightID;
    private int boatCounter = 0;

	// Use this for initialization
	void Start () {
        controller = this.transform.root.GetComponent<LightsController>();
        lightID = this.transform.parent.gameObject.GetComponent<TrafficLightBehaviour>().lightID;
	}

    private void OnTriggerEnter(Collider other)
    {
        if(lightID[0] == '4' && other.gameObject.GetComponentInParent<CarBehavior>().road == lightID)
            {
                boatCounter++;
                Debug.Log("Amount of boats in " + lightID + ": " + boatCounter);
        }
        controller.TriggerSignal(lightID, triggerID, true);
        other.gameObject.GetComponentInParent<CarBehavior>().hitPrimaryTrigger = true;
    }

    private void OnTriggerExit(Collider other)
    {
        if(lightID[0] == '4')
        {
            if (other.gameObject.GetComponentInParent<CarBehavior>().road == lightID)
            {
                boatCounter--;
                Debug.Log("Amount of boats in " + lightID + ": " + boatCounter);
                if (boatCounter == 0)
                    controller.TriggerSignal(lightID, triggerID, false);
            }
        }
        else
            controller.TriggerSignal(lightID, triggerID, false);
    }
}
