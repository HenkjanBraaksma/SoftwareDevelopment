using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GoalRotationTrigger : MonoBehaviour {

    public float goalRotation = 0;
    public float rotationSpeed = 0;

    private void OnTriggerEnter(Collider other)
    {
        CarBehavior car = other.gameObject.GetComponent<CarBehavior>();
        float carRotation = other.gameObject.transform.rotation.eulerAngles.y;
        if(carRotation != goalRotation)
        {
            float rotationDifference = goalRotation - carRotation;
            car.SetNewRotation(rotationDifference, rotationSpeed);
        }
    }

        // Use this for initialization
        void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}
}
