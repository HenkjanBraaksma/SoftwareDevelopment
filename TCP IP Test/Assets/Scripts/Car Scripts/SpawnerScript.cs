using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SpawnerDetails
{
    public string road;
    public string vehicle;
    public Vector3 position;
    public Quaternion rotation;

    private float spawnY = 202.8f; //The ideal spawn height for cars

    public SpawnerDetails(string roadInput, string vehicleType, float spawnX, float spawnZ, Quaternion rotationInput)
    {
        spawnY = 202.8f;
        position = new Vector3(spawnX, spawnY, spawnZ);
        road = roadInput;
        rotation = rotationInput;
        vehicle = vehicleType;
    }
}

public class SpawnerScript : MonoBehaviour {

    public List<GameObject> vehicles;
    private List<SpawnerDetails> spawns;
    private float spawnY = 202.8f;
    public bool isOn = true;

    private string lastVehicle = "";
    private string secondToLastVehicle = "";

    private GameObject Human;
    private GameObject Boat;
    private GameObject Bike;

    // Use this for initialization
    void Start () {
        vehicles = new List<GameObject>();
        spawns = new List<SpawnerDetails>();

        GameObject RedCar = Resources.Load("Prefabs/RedCar") as GameObject;
        vehicles.Add(RedCar);
        GameObject YellowCar = Resources.Load("Prefabs/YellowCar") as GameObject;
        vehicles.Add(YellowCar);
        GameObject PoliceCar = Resources.Load("Prefabs/PoliceCar") as GameObject;
        vehicles.Add(PoliceCar);
        GameObject YellowBus = Resources.Load("Prefabs/YellowBus") as GameObject;
        vehicles.Add(YellowBus);
        GameObject BlueBus = Resources.Load("Prefabs/BlueBus") as GameObject;
        vehicles.Add(BlueBus);
        GameObject Taxi = Resources.Load("Prefabs/Taxi") as GameObject;
        vehicles.Add(Taxi);
        GameObject GreenCar = Resources.Load("Prefabs/GreenCar") as GameObject;
        vehicles.Add(GreenCar);
        GameObject BlueCar = Resources.Load("Prefabs/BlueCar") as GameObject;
        vehicles.Add(BlueCar);

        Human = Resources.Load("Prefabs/Human Person") as GameObject;
        Boat = Resources.Load("Prefabs/Boat North") as GameObject;
        Bike = Resources.Load("Prefabs/Bicycle") as GameObject;


        //Cars
        spawns.Add(new SpawnerDetails("1.4", "CAR", 158f, -6.6f, Quaternion.Euler(0, 20, 0)));
        spawns.Add(new SpawnerDetails("1.5", "CAR", 158f, -6.6f, Quaternion.Euler(0, 20, 0)));
        spawns.Add(new SpawnerDetails("1.6", "CAR", 158f, -6.6f, Quaternion.Euler(0, 20, 0)));
        spawns.Add(new SpawnerDetails("1.7", "CAR", -1.1f, 316.9f, Quaternion.Euler(0, 90.7f, 0)));
        spawns.Add(new SpawnerDetails("1.8", "CAR", -0.7f, 328.9f, Quaternion.Euler(0, 90.7f, 0)));
        spawns.Add(new SpawnerDetails("1.8", "CAR", -0.7f, 342.4f, Quaternion.Euler(0, 90.7f, 0)));
        spawns.Add(new SpawnerDetails("1.9", "CAR", -1.1f, 354f, Quaternion.Euler(0, 90.7f, 0)));
        spawns.Add(new SpawnerDetails("1.10", "CAR", 163.8f, 517.3f, Quaternion.Euler(0, 179.5f, 0)));
        spawns.Add(new SpawnerDetails("1.11", "CAR", 179f, 517.4f, Quaternion.Euler(0, 179.5f, 0)));
        spawns.Add(new SpawnerDetails("1.12", "CAR", 193f, 517.5f, Quaternion.Euler(0, 179.5f, 0)));
        spawns.Add(new SpawnerDetails("1.13", "CAR", 2016.7f, 193.1f, Quaternion.Euler(0, 281, 0)));

        //Bikes
        spawns.Add(new SpawnerDetails("A2A1", "BIKE", 7.5f, 293.7f, Quaternion.Euler(0, 91, 0)));
        spawns.Add(new SpawnerDetails("A2B1", "BIKE", 7.5f, 293.7f, Quaternion.Euler(0, 91, 0)));
        spawns.Add(new SpawnerDetails("A2C1", "BIKE", 7.5f, 293.7f, Quaternion.Euler(0, 91, 0)));
        spawns.Add(new SpawnerDetails("A2D1", "BIKE", 7.5f, 293.7f, Quaternion.Euler(0, 91, 0)));
        spawns.Add(new SpawnerDetails("A2D2", "BIKE", 7.5f, 293.7f, Quaternion.Euler(0, 91, 0)));

        spawns.Add(new SpawnerDetails("B2A1", "BIKE", 151.3f, 507.3f, Quaternion.Euler(0, 180, 0)));
        spawns.Add(new SpawnerDetails("B2B1", "BIKE", 151.3f, 507.3f, Quaternion.Euler(0, 180, 0)));
        spawns.Add(new SpawnerDetails("B2C1", "BIKE", 151.3f, 507.3f, Quaternion.Euler(0, 180, 0)));
        spawns.Add(new SpawnerDetails("B2D1", "BIKE", 151.3f, 507.3f, Quaternion.Euler(0, 180, 0)));
        spawns.Add(new SpawnerDetails("B2D2", "BIKE", 151.3f, 507.3f, Quaternion.Euler(0, 180, 0)));

        spawns.Add(new SpawnerDetails("C2A1", "BIKE", 2041.8f, 212.69f, Quaternion.Euler(0, 281, 0)));
        spawns.Add(new SpawnerDetails("C2B1", "BIKE", 2041.8f, 212.69f, Quaternion.Euler(0, 281, 0)));
        spawns.Add(new SpawnerDetails("C2C1", "BIKE", 2041.8f, 212.69f, Quaternion.Euler(0, 281, 0)));
        spawns.Add(new SpawnerDetails("C2D1", "BIKE", 2041.8f, 212.69f, Quaternion.Euler(0, 281, 0)));
        spawns.Add(new SpawnerDetails("C2D2", "BIKE", 2041.8f, 212.69f, Quaternion.Euler(0, 281, 0)));

        spawns.Add(new SpawnerDetails("D2A1", "BIKE", 222.2f, 7.7f, Quaternion.Euler(0, 27.5f, 0)));
        spawns.Add(new SpawnerDetails("D2B1", "BIKE", 222.2f, 7.7f, Quaternion.Euler(0, 27.5f, 0)));
        spawns.Add(new SpawnerDetails("D2C1", "BIKE", 222.2f, 7.7f, Quaternion.Euler(0, 27.5f, 0)));
        spawns.Add(new SpawnerDetails("D2D1", "BIKE", 222.2f, 7.7f, Quaternion.Euler(0, 27.5f, 0)));

        //Humans
        spawns.Add(new SpawnerDetails("HA1A2", "HUMAN", 1.7f, 427.7f, Quaternion.Euler(0, 90, 0)));

        spawns.Add(new SpawnerDetails("HA2A1", "HUMAN", 1.7f, 281f, Quaternion.Euler(0, 90, 0)));
        spawns.Add(new SpawnerDetails("HA2B1", "HUMAN", 1.7f, 284f, Quaternion.Euler(0, 90, 0)));
        spawns.Add(new SpawnerDetails("HA2D2", "HUMAN", 1.7f, 284f, Quaternion.Euler(0, 90, 0)));

        spawns.Add(new SpawnerDetails("HB1A1", "HUMAN", 260.4f, 509.6f, Quaternion.Euler(0, 165, 0)));
        spawns.Add(new SpawnerDetails("HB1A2", "HUMAN", 260.4f, 509.6f, Quaternion.Euler(0, 165, 0)));
        spawns.Add(new SpawnerDetails("HB1D2", "HUMAN", 260.4f, 509.6f, Quaternion.Euler(0, 165, 0)));

        spawns.Add(new SpawnerDetails("HB2B1", "HUMAN", 140.6f, 508.6f, Quaternion.Euler(0, 195, 0)));


        //Boats
        spawns.Add(new SpawnerDetails("4.2", "BOAT", 1989f, 486f, Quaternion.Euler(0, 215, 0)));
        spawns.Add(new SpawnerDetails("4.1", "BOAT", 1709f, 21f, Quaternion.Euler(0, 35, 0)));

        if (isOn)
            InvokeRepeating("SpawnCar", 5f, 2f);
	}

    void SpawnCar()
    {
        bool spawned = false;
        int attempts = 0;
        GameObject vehicleData;
        while(!spawned && attempts < 20)
        {
            SpawnerDetails spawn = spawns[Random.Range(0, spawns.Count)];

            if(true)//spawn.vehicle != lastVehicle && spawn.vehicle != secondToLastVehicle)
            {
                switch (spawn.vehicle)
                {
                    case "BIKE":
                        vehicleData = Bike;
                        break;
                    case "BOAT":
                        vehicleData = Boat;
                        break;
                    case "HUMAN":
                        vehicleData = Human;
                        break;
                    default:
                        //Default generates cars
                        vehicleData = vehicles[Random.Range(0, vehicles.Count)];
                        break;
                }

                CarBehavior newVehicle = vehicleData.GetComponent<CarBehavior>();
                newVehicle.road = spawn.road;

                switch (newVehicle.identity)
                {
                    case "BUS":
                        spawn.position.y = (spawnY + 1.2f);
                        break;
                    case "BIKE":
                        spawn.position.y = (spawnY - 2f);
                        break;
                    case "BOAT":
                        spawn.position.y = (spawnY - 6f);
                        break;
                    case "HUMAN":
                        spawn.position.y = (spawnY - 1.4f);
                        break;
                    default:
                        break;
                }
                Instantiate(vehicleData, spawn.position, spawn.rotation);
                secondToLastVehicle = lastVehicle;
                lastVehicle = newVehicle.identity;

                spawned = true;
            }
        }
    }
}
