using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CarBehavior : MonoBehaviour {

    private BoxCollider ownCollider;
    private float currentSpeed = 45;
    private bool roadOpen = true;
    private bool noCar = true;

	// Use this for initialization
	void Start () {
        ownCollider = GetComponent<BoxCollider>();
	}
	
	// Update is called once per frame
	void Update () {
        Debug.DrawRay(transform.position, transform.forward * 12, Color.red, Time.deltaTime, false);
        CheckCars();
        if (roadOpen && noCar)
            Move();
        else
            Brake();
    }

    void Move()
    {
        Vector3 forward = new Vector3(0, 0, currentSpeed);
        transform.Translate(forward * Time.deltaTime);

        if (!(currentSpeed >= 45))
        {
            currentSpeed += Random.Range(1, 7);
        }
        if (currentSpeed > 45)
        {
            currentSpeed = 45;
        }
    }

    void Brake()
    {
        if (currentSpeed > 0)
        {
            currentSpeed -= 5;
            if (currentSpeed < 2)
                currentSpeed = 0;

            Vector3 forward = new Vector3(0, 0, currentSpeed);
            transform.Translate(forward * Time.deltaTime);
        }
    }

    private void OnTriggerEnter(Collider other)
    {
        if(other.gameObject.name == "StopLine")
        {
            Debug.Log("Nice");
            if (other.gameObject.GetComponentInParent<TrafficLightBehaviour>().lightStatus == "red")
                roadOpen = false;
        }
    }

    private void OnTriggerStay(Collider other)
    {
        if(other.gameObject.name == "StopLine" && !roadOpen)
        {
            if (other.gameObject.GetComponentInParent<TrafficLightBehaviour>().lightStatus == "green")
                roadOpen = true;
        }
    }

    private void CheckCars()
    {
        RaycastHit hitInfo;
        Ray lookAhead = new Ray(transform.position, transform.forward);
        if (noCar && Physics.Raycast(lookAhead, out hitInfo, 19f))
        {
            if (hitInfo.collider.tag == "Car" && hitInfo.collider != ownCollider)
                noCar = false;
        }
        else if (!noCar && Physics.Raycast(lookAhead, out hitInfo, 28f))
            noCar = false;
        else
            noCar = true;
    }
}
