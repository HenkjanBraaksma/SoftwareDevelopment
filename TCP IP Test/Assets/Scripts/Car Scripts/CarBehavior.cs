using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CarBehavior : MonoBehaviour {

    public float topSpeed = 500;
    public float topReverse = -100;

    public float acceleration = 1.1f;
    public float reverseAccelerate = 1.05f;

    private Rigidbody rb;
    private float currentSpeed = 0;

	// Use this for initialization
	void Start () {
        rb = GetComponent<Rigidbody>();
	}
	
	// Update is called once per frame
	void Update () {
	}

    void FixedUpdate()
    {
        if (Input.GetButton("Forward"))
            Accelerate();
    }

    void Accelerate()
    {
        if (currentSpeed < 1)
            currentSpeed = 1;

        rb.AddRelativeForce(Vector3.forward * currentSpeed);

        if (currentSpeed < topSpeed)
            currentSpeed *= acceleration;
    }

    void Reverse()
    {
    }
}
