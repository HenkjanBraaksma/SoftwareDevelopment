using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CarBehavior : MonoBehaviour {

    public string road = "";
    public string identity = "";
    public int maxSpeed = 45;
    public bool hitPrimaryTrigger = false;

    private BoxCollider ownCollider;
    private float currentSpeed = 45;
    private bool roadOpen = true;
    private bool noCar = true;
    private float targetYRotation;
    private float resultRotation;
    private float rotationSpeed;
    private float rayLength;

    public void SetNewRotation(float newAngle, float newSpeed)
    {
        resultRotation = transform.rotation.eulerAngles.y + newAngle;
        if (newSpeed == 0)
        {
            transform.rotation = Quaternion.Euler(0, resultRotation, 0);
        }
        else
        {
            targetYRotation = newAngle;
            rotationSpeed = newSpeed;
        }
    }

    // Use this for initialization
    void Start() {
        ownCollider = GetComponent<BoxCollider>();
        resultRotation = transform.rotation.eulerAngles.y;
        if (identity == "CAR" || identity == "BUS" || identity == "BOAT")
            rayLength = 16;
        else
            rayLength = 4;
    }

    // Update is called once per frame
    void Update() {
        Debug.DrawRay(transform.position, transform.forward * rayLength, Color.red, Time.deltaTime, false);
        if (identity == "CAR" || identity == "BUS")
        {
            Debug.DrawRay(new Vector3(transform.position.x - 3, transform.position.y, transform.position.z), transform.forward * rayLength, Color.red, Time.deltaTime, false);
            Debug.DrawRay(new Vector3(transform.position.x + 3, transform.position.y, transform.position.z), transform.forward * rayLength, Color.red, Time.deltaTime, false);
            Debug.DrawRay(new Vector3(transform.position.x, transform.position.y, transform.position.z - 3), transform.forward * rayLength, Color.red, Time.deltaTime, false);
            Debug.DrawRay(new Vector3(transform.position.x, transform.position.y, transform.position.z + 3), transform.forward * rayLength, Color.red, Time.deltaTime, false);
        }


        CheckCars();
        if (roadOpen && noCar)
        {
            Move();
        }
        else
            Brake();
        RemoveCheck();
    }

    void Move()
    {
        Vector3 forward = new Vector3(0, 0, currentSpeed);
        transform.Translate(forward * Time.deltaTime);

        if (!(currentSpeed >= maxSpeed))
        {
            currentSpeed += Random.Range(1, 7);
        }
        if (currentSpeed > maxSpeed)
        {
            currentSpeed = maxSpeed;
        }
        Turn();
    }

    void Brake()
    {
        if (currentSpeed > 0)
        {
            currentSpeed -= 6;
            if (currentSpeed < 2)
                currentSpeed = 0;

            Vector3 forward = new Vector3(0, 0, currentSpeed);
            transform.Translate(forward * Time.deltaTime);
        }
    }

    void Turn()
    {
        float currentRotation = transform.rotation.eulerAngles.y;
        float relativeRotation = 0;

        if (targetYRotation < -1 || targetYRotation > 1)
        {
            if (relativeRotation > targetYRotation)
            {
                currentRotation -= rotationSpeed * Time.deltaTime;
                transform.rotation = Quaternion.Euler(0, currentRotation, 0);
                targetYRotation += rotationSpeed * Time.deltaTime;
            }
            if (relativeRotation < targetYRotation)
            {
                currentRotation += rotationSpeed * Time.deltaTime;
                transform.rotation = Quaternion.Euler(0, currentRotation, 0);
                targetYRotation -= rotationSpeed * Time.deltaTime;
            }
        }
        else
        {
            transform.rotation = Quaternion.Euler(0, resultRotation, 0);
        }
    }

    private void OnTriggerEnter(Collider other)
    {
        GameObject otherObject = other.gameObject;
        TrafficLightBehaviour light = otherObject.GetComponentInParent<TrafficLightBehaviour>();
        if(otherObject.name == "StopLine" && hitPrimaryTrigger)
        {
            if(identity == "CAR" || identity == "BUS" || light.lightID == "1.13")
            {
                if (light.lightID == road || light.lightID == "1.13")
                {
                    if (light.lightStatus == "red" || light.lightStatus == "orange")
                        roadOpen = false;
                }
            }
            else if (identity == "BIKE" && light.lightID[0] == '2')
            {
                if (light.lightStatus == "red")
                    roadOpen = false;
            }
            
            else if(identity == "HUMAN" && light.lightID[0] == '3')
            {
                if (light.lightStatus == "red")
                    roadOpen = false;
            }

            else if (identity == "BOAT" && light.lightID == road)
            {
                if (light.lightStatus == "red")
                    roadOpen = false;
            }
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

    private void OnTriggerExit(Collider other)
    {
        if(other.gameObject.name == "StopLine")
        {
            hitPrimaryTrigger = false;
        }
    }

    private void CheckCars()
    {
        RaycastHit hitInfo;
        List<Ray> rayCasts = new List<Ray>();
        bool noCarSpotted = true;
        Vector3 rayPosition = transform.position;
        if(identity == "BOAT")
        {
            rayPosition.y += 0.5f;
        }

        Ray lookAhead = new Ray(rayPosition, transform.forward);
        rayCasts.Add(lookAhead);

        if(identity == "CAR" || identity == "BUS")
        {
            Vector3 lookAheadOrigin = lookAhead.origin;
            lookAhead = new Ray(new Vector3(lookAheadOrigin.x + 3, lookAheadOrigin.y, lookAheadOrigin.z), transform.forward);
            rayCasts.Add(lookAhead);
            lookAhead = new Ray(new Vector3(lookAheadOrigin.x - 3, lookAheadOrigin.y, lookAheadOrigin.z), transform.forward);
            rayCasts.Add(lookAhead);
            lookAhead = new Ray(new Vector3(lookAheadOrigin.x, lookAheadOrigin.y, lookAheadOrigin.z + 3), transform.forward);
            rayCasts.Add(lookAhead);
            lookAhead = new Ray(new Vector3(lookAheadOrigin.x, lookAheadOrigin.y, lookAheadOrigin.z + 3), transform.forward);
            rayCasts.Add(lookAhead);
        }

       foreach (Ray r in rayCasts)
       {
            if (Physics.Raycast(r, out hitInfo, rayLength))
            {
                    if (hitInfo.collider.tag == "Car" && hitInfo.collider != ownCollider)
                        noCarSpotted = false;
            }
        }
        noCar = noCarSpotted;
    }

    private void RemoveCheck()
    {
        if(transform.position.y < 195)
        {
            Object.Destroy(this.gameObject);
        }
    }
}
