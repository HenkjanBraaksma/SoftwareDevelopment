using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TriggerScript : MonoBehaviour {

    public int triggerID = 1;

    private LightsController controller;
    private string lightID;

	// Use this for initialization
	void Start () {
        controller = this.transform.root.GetComponent<LightsController>();
        lightID = this.transform.parent.gameObject.GetComponent<TrafficLightBehaviour>().lightID;
	}

    private void OnTriggerEnter(Collider other)
    {
        controller.TriggerSignal(lightID, triggerID);
    }
}
