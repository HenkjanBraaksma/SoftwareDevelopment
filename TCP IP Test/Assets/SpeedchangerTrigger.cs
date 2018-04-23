using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SpeedchangerTrigger : MonoBehaviour {

    public int SpeedSetting = 45;

    private void OnTriggerEnter(Collider other)
    {
        CarBehavior car = other.gameObject.GetComponent<CarBehavior>();
        car.maxSpeed = SpeedSetting;
    }

}
