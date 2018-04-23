using System.Collections;
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
        Debug.Log("Trigger " + triggerID + " of light " + lightID + " triggered");
        if(lightID[0] == 4)
        {
            boatCounter++;
        }
        controller.TriggerSignal(lightID, triggerID, true);
    }

    private void OnTriggerExit(Collider other)
    {
        if(lightID[0] == 4)
        {
            boatCounter--;
            if (boatCounter == 0)
                controller.TriggerSignal(lightID, triggerID, false);
        }
        else
            controller.TriggerSignal(lightID, triggerID, false);
    }
}
