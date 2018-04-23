using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LaneChangeTrigger : MonoBehaviour {

    public string roads = "1.13";

    private void OnTriggerEnter(Collider other)
    {
        CarBehavior car = other.gameObject.GetComponent<CarBehavior>();
        string[] lanes = roads.Split(',');

        car.road = lanes[Random.Range(0, lanes.Length)];

    }
}
