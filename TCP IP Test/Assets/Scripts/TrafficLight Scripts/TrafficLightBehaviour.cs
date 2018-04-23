using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TrafficLightBehaviour : MonoBehaviour {

    public string lightID = "1.2";
    public string lightStatus = "red";

    private Material red;
    private Material orange;
    private Material green;

    private new Renderer renderer;

    public void Start()
    {
        red = Resources.Load("Materials/Red", typeof(Material)) as Material;
        orange = Resources.Load("Materials/Orange", typeof(Material)) as Material;
        green = Resources.Load("Materials/Green", typeof(Material)) as Material;
        renderer = GetComponent<Renderer>();
    }

    public void ChangeLight(string light)
    {
        Debug.Log("Light: Changed!");

        switch(light)
        {
            case "red":
                lightStatus = light;
                renderer.material = red;
                break;
            case "orange":
                lightStatus = light;
                renderer.material = orange;
                break;
            case "green":
                lightStatus = light;
                renderer.material = green;
                break;
            default:
                Debug.Log("Error: Traffic light signal not red/orange/green");
                break;
        }
    }

}
