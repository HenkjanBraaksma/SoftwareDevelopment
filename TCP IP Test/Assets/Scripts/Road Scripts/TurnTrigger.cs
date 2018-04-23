using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TurnTrigger : MonoBehaviour {

    public string road = "1.1";
    public float rotationAngle = 0;
    public float rotationSpeed = 0;

    public bool alternateRoad = false;
    public float alternateAngle = 0;
    public float alternateSpeed = 0;

    private void OnTriggerEnter(Collider other)
    {
        CarBehavior car = other.gameObject.GetComponent<CarBehavior>();
        if(car.road == road)
        {
            if(alternateRoad)
            {
                if (Random.Range(0, 2) == 1)
                    car.SetNewRotation(alternateAngle, alternateSpeed);
                else
                    car.SetNewRotation(rotationAngle, rotationSpeed);

            }
            else
                car.SetNewRotation(rotationAngle, rotationSpeed);
        }
    }
}
