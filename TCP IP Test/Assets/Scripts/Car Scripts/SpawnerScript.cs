using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SpawnerScript : MonoBehaviour {

    GameObject RedCar;
    GameObject YellowCar;
    GameObject PoliceCar;
    GameObject YellowBus;

	// Use this for initialization
	void Start () {
        RedCar = GameObject.Find("RedCar");
        InvokeRepeating("SpawnCar", 5f, 2f);
	}
	
	// Update is called once per frame
	void Update () {
		
	}

    void SpawnCar()
    {
        Instantiate(RedCar, new Vector3(603.7f, 3.72f, 371.3f),Quaternion.Euler(0, 270, 0));
    }
}
