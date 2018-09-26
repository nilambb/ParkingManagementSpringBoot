package parking.management.app;

import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import parking.management.configuration.ParkingConfiguration;
import parking.management.constant.ParkingConstants;
import parking.management.exception.InvalidConsoleInput;
import parking.management.exception.InvalidParking;
import parking.management.exception.InvalidParkingType;
import parking.management.exception.InvalidTicket;
import parking.management.exception.InvalidVehicleType;
import parking.management.model.ParkingLot;
import parking.management.model.Ticket;
import parking.management.model.Vehicle;
import parking.management.service.ParkingService;
import parking.management.util.ParkingUtil;

public class ParkingApplication {

	private static final Logger logger = LogManager.getLogger(ParkingApplication.class);
	@Autowired
	private ParkingService service;
	public static boolean isStop = false;

	public static void main(String[] args) {
		System.out.println("Starting the Parking Management system...");
		ApplicationContext context = new AnnotationConfigApplicationContext(ParkingConfiguration.class);
		ParkingApplication application = (ParkingApplication) context.getBean("parkingApplication");
		System.out.println("Parking Management system started");
		logger.info("Parking service is star1ted.....");

		while (!(isStop)) {
			application.manageParking(context);
		}
		if (context != null) {
			((AnnotationConfigApplicationContext) context).close();
		}
		System.out.println("You have stopped the valate parking service..... Good Bye!!!!...........");
		logger.info("Stopped the parking service....");
	}

	public void manageParking(ApplicationContext context) {
		int valetChoice = 0;
		System.out.println("\n\n===========================================================");
		System.out.println("\nPress 1 : Parking is totally empty");
		System.out.println("\nPress 2 : Parking is full");
		System.out.println("\nPress 3 : Parking is available");
		System.out.println("\nPress 4 : Allocate parking");
		System.out.println("\nPress 5 : De-allocate parking");
		System.out.println("\nPress 6 : Stop the application........");
		System.out.println("===========================================================");

		// Reading data using readLine
		try {
			System.out.println("\n\n--------------Enter you choice-----------------");
			String input = ParkingUtil.getInputFromConsole();
			valetChoice = Integer.parseInt(input);
		} catch (Exception e) {
			System.out.println("An error occurred while getting the input. Please enter a valid number");
		}

		try {
			porocessParkingRequests(valetChoice, context);
		} catch (InvalidConsoleInput | IOException e) {
			System.out.println("You have entered wrong data. Please make valid selections...");
			logger.error("An exception occrred.. User entered wrong data. Please make valid selections...", e);
		} catch (InvalidVehicleType e) {
			System.out.println(
					"The vehicle type is not valid can not process the request. Please enter the valid vehicle type");
			logger.error("The enetered vehicle type in not suported. ", e);
		} catch (InvalidParkingType e) {
			System.out.println("Invalid parking type. Please make valid selections. Can not process the request");
			logger.error("An invalid parking is selected... ", e);
		} catch (InvalidParking e) {
			System.out.println("Invalid parking request. Can not process the parking request");
			logger.error("An error occurred while processing the parking request..", e);
		} catch (InvalidTicket e) {
			System.out.println("Ticket is not present in system. Invalid ticket.");
			logger.error("Invalid ticket data, can not process the request..", e);
		} catch (Exception e) {
			System.out
					.println("An problem occurred while processing the request please try again and enter valid data.");
			logger.error("An exception occurred while processing the request ", e);

		}
	}

	public void porocessParkingRequests(int valetChoice, ApplicationContext context) throws InvalidConsoleInput,
			IOException, InvalidVehicleType, InvalidParkingType, InvalidParking, InvalidTicket {
		switch (valetChoice) {
		case 1:
			if (service.isParkingEmpty()) {
				System.out.println("\n\n***********************Parking is empty***********************");
			} else {
				System.out.println(
						"\n\n*********************Parking is not empty some vehicle are still parked in **********************");
			}
			break;
		case 2:
			if (service.isParkingFull()) {
				System.out.println("\n\n*****************Parking is full. No space available for parking***************");
			} else {
				System.out.println("\n\n******************Parking is not full*******************************");
			}
			break;
		case 3:
			checkAvailablity(context);
			break;
		case 4:
			allocateParking(context);
			break;
		case 5:
			deallocateParking(context);
			break;
		case 6:
			System.out.println(
					"\nStop request is generated parform clean up activity and stopping parking management service..... ");
			service.systemCleanUp();
			isStop = true;
			logger.info("User requested to stop the serice.... Good Bye!!!...............");
			break;
		default:
			System.out.println("\n.....Entered invalid choice. Please rentered the correct choice............");
			logger.debug("User entered the wrong choice for parking request...." + valetChoice);

		}
		/*
		 * Pausing the application so that the output is visible clearly
		 * 
		 * */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("\n\nA problem occurred while processing the service");
			logger.error("An exception occerred while pausing the thread " + e);
		}
	}

	private void checkAvailablity(ApplicationContext context)
			throws InvalidConsoleInput, IOException, InvalidVehicleType, InvalidParkingType, InvalidParking {
		if (service.isParkingAvailable(ParkingUtil.getParkingTypeFromConsole().toLowerCase(),
				ParkingUtil.getVehicleTypeFromConsole())) {
			System.out.println("\n\n**********************The parking is available*************");
		} else {
			System.out.println(
					"\n\n************Sorry the specified parking is not available at this time. Try some time later************");
		}
	}

	private void allocateParking(ApplicationContext context)
			throws InvalidConsoleInput, IOException, InvalidVehicleType, InvalidParkingType, InvalidParking {
		ParkingLot lot;
		String parkingType = ParkingUtil.getParkingTypeFromConsole();
		switch (parkingType) {
		case "compact":
			lot = ParkingLot.COMPACT;
			break;
		case "handicapped":
			lot = ParkingLot.HANDICAPPED;
			break;
		default:
			lot = ParkingLot.REGULAR;
		}
		String vehicleType = ParkingUtil.getVehicleTypeFromConsole();
		String vehicleNumber;
		System.out.println("Enter the vehicle number");
		vehicleNumber = ParkingUtil.getInputFromConsole();
		Vehicle vehicle = createVehicle(vehicleNumber, vehicleType, context);

		Ticket ticket = service.allocateParking(lot, vehicle);
		System.out.println("\n\n************************************************************************************");
		System.out.println("Ticket's details are : \n");
		System.out.println(ticket.toString());
		System.out.println("************************************************************************************");
	}

	/**
	 * 
	 * @param service
	 * @param context
	 * @throws IOException
	 * @throws InvalidTicket
	 * @throws InvalidParking
	 */
	private void deallocateParking(ApplicationContext context) throws IOException, InvalidTicket, InvalidParking {
		String input;
		Vehicle vehicle = null;
		System.out.println("Enter the ticket numeber : ");
		input = ParkingUtil.getInputFromConsole();
		int ticketId = Integer.parseInt(input);

		vehicle = service.deallocateParking(ticketId);
		System.out.println("\n=========================================================================");
		System.out.println("Here is the vehicle de-allocating parking is \n" + vehicle.toString());
		System.out.println("==========================================================================");
	}

	/**
	 * 
	 * @param vehicleNumber
	 * @param vehicleType
	 * @param context
	 * @return
	 * @throws InvalidVehicleType
	 */
	public static Vehicle createVehicle(String vehicleNumber, String vehicleType, ApplicationContext context)
			throws InvalidVehicleType {
		Vehicle vehicle = null;
		switch (vehicleType) {
		case ParkingConstants.SMALL_VEHICLE_TYPE:
			vehicle = (Vehicle) context.getBean("smallVehicle");
			break;
		case ParkingConstants.MEDIUM_VEHICLE_TYPE:
			vehicle = (Vehicle) context.getBean("mediumVehicle");
			break;
		case ParkingConstants.LARGE_VEHICLE_TYPE:
			vehicle = (Vehicle) context.getBean("largeVehicle");
			break;
		default:
			throw new InvalidVehicleType("The vehicle type passed to create a vehicle is not valid.");
		}
		vehicle.setArrivalTime(new Date());
		vehicle.setVehicleNumber(vehicleNumber);
		return vehicle;
	}
}