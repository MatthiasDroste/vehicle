package com.droste.vehicle.rest;

import static com.droste.vehicle.rest.SessionRestControllerTest.OLDEST;
import static com.droste.vehicle.rest.SessionRestControllerTest.SESSION_ID;
import static com.droste.vehicle.rest.SessionRestControllerTest.SESSION_ID2;
import static com.droste.vehicle.rest.SessionRestControllerTest.TIMESTAMP;
import static com.droste.vehicle.rest.SessionRestControllerTest.VIN;
import static com.droste.vehicle.rest.SessionRestControllerTest.VIN2;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.droste.vehicle.VehicleApplication;
import com.droste.vehicle.model.Position;
import com.droste.vehicle.model.PositionRepository;
import com.droste.vehicle.model.Session;
import com.droste.vehicle.model.SessionRepository;
import com.droste.vehicle.model.Vehicle;
import com.droste.vehicle.model.VehicleRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VehicleApplication.class)
@WebAppConfiguration
public class VehicleRestControllerTest {

	private Position testPosition;
	private Vehicle testVehicle;

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;

	@SuppressWarnings("rawtypes")
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private VehicleRepository vehicleRepository;
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private PositionRepository positionRepository;

	/** always have 1 vehicle with 1 session and 1 position in the db */
	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.testVehicle = vehicleRepository.save(new Vehicle(VIN));

		Session session = new Session(SESSION_ID, TIMESTAMP, testVehicle);
		session = sessionRepository.save(session);

		this.testPosition = new Position(TIMESTAMP, 48.1167D, 11.5400D, 252, session);
		positionRepository.save(testPosition);
	}

	@After
	public void tearDown() throws Exception {
		this.positionRepository.deleteAllInBatch();
		this.sessionRepository.deleteAllInBatch();
		this.vehicleRepository.deleteAllInBatch();
	}

	@Test
	public void pageNotFound() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isNotFound());
	}

	@Test
	public void vehicleNotFound() throws Exception {
		mockMvc.perform(get("/vehicle/123")).andExpect(status().isNotFound());
	}

	@Test
	public void getVehicle() throws Exception {
		mockMvc.perform(get("/vehicle/" + VIN)).andExpect(status().isOk()).andExpect(content().contentType(contentType))
				.andExpect(jsonPath("$.id", is(VIN))).andExpect(jsonPath("$.sessions[0].sessionId", is(SESSION_ID)));
	}

	@Test
	public void getLastPosition() throws Exception {
		mockMvc.perform(get("/vehicle/" + VIN + "/position/latest")).andExpect(status().isOk())
				.andExpect(content().contentType(contentType)).andExpect(jsonPath("$.timestamp", is(TIMESTAMP)));
	}

	/**
	 * add a existing position to existing session and vehicle
	 */
	@Test
	public void postDuplicatePosition() throws Exception {
		String content = json(testPosition);
		this.mockMvc.perform(post("/vehicle/" + VIN + "/session/" + SESSION_ID + "/position").contentType(contentType)
				.content(content)).andExpect(status().isConflict());

		Vehicle vehicle = this.vehicleRepository.findOne(VIN);
		assertThat(vehicle.getId(), is(VIN));
		assertThat(vehicle.getSessions().size(), is(1));

		Session session = vehicle.getSessions().get(0);
		assertThat(session.getSessionId(), is(SESSION_ID));
		assertThat(session.getTimestamp(), is(testPosition.getTimestamp()));

		List<Position> positions = this.positionRepository.findAllBySessionSessionId(SESSION_ID);
		assertThat(positions.size(), is(1));
		assertThat(positions.get(0).getTimestamp(), is(testPosition.getTimestamp()));
	}

	/**
	 * add a new position to existing session and vehicle
	 */
	@Test
	public void postNewPosition() throws Exception {
		Position testPosition2 = new Position(OLDEST, 48.1167D, 11.5394D, 291,
				this.sessionRepository.findOne(SESSION_ID));
		String content = json(testPosition2);
		this.mockMvc.perform(post("/vehicle/" + VIN + "/session/" + SESSION_ID + "/position").contentType(contentType)
				.content(content)).andExpect(status().isCreated());

		Vehicle vehicle = this.vehicleRepository.findOne(VIN);
		assertThat(vehicle.getId(), is(VIN));
		assertThat(vehicle.getSessions().size(), is(1));

		Session session = vehicle.getSessions().get(0);
		assertThat(session.getSessionId(), is(SESSION_ID));
		assertThat(session.getTimestamp(), is(testPosition.getTimestamp()));

		List<Position> positions = this.positionRepository.findAllBySessionSessionId(SESSION_ID);
		assertThat(positions.size(), is(2));
		assertThat(positions.get(0).getTimestamp(), is(testPosition.getTimestamp()));
		assertThat(positions.get(1).getTimestamp(), is(OLDEST));
	}

	/**
	 * add a new position, neither session nor vehicle exist
	 */
	@Test
	public void postNewVehicle() throws Exception {
		Vehicle vehicle2 = new Vehicle(VIN2, SESSION_ID2);
		Position testPosition2 = new Position(OLDEST, 48.1167D, 11.5394D, 291, vehicle2.getSession(SESSION_ID2));
		String content = json(testPosition2);
		this.mockMvc.perform(post("/vehicle/" + VIN2 + "/session/" + SESSION_ID2 + "/position").contentType(contentType)
				.content(content)).andExpect(status().isCreated());

		Vehicle vehicle = this.vehicleRepository.findOne(VIN2);
		assertThat(vehicle.getSessions().size(), is(1));

		Session session = vehicle.getSessions().get(0);
		assertEquals(session.getSessionId(), SESSION_ID2);
		assertEquals(session.getTimestamp(), OLDEST);

		// get Positions only on explicit request - performance!
		List<Position> positions = positionRepository.findAllBySessionSessionId(SESSION_ID2);
		assertEquals(positions.size(), 1);
		assertEquals(positions.get(0).getTimestamp(), OLDEST);
	}

	/**
	 * add a new position, session is new, vehicle exists
	 */
	@Test
	public void postNewSession() throws Exception {
		Session session2 = new Session(SESSION_ID2, testVehicle);
		Position testPosition2 = new Position(OLDEST, 48.1167D, 11.5394D, 291, session2);
		String content = json(testPosition2);
		this.mockMvc.perform(post("/vehicle/" + VIN + "/session/" + SESSION_ID2 + "/position").contentType(contentType)
				.content(content)).andExpect(status().isCreated());

		Vehicle vehicle = this.vehicleRepository.findOne(VIN);
		assertEquals(vehicle.getId(), VIN);
		assertEquals(vehicle.getSessions().size(), 2);

		Session session = vehicle.getSessions().get(1);
		assertEquals(session.getSessionId(), SESSION_ID2);
		assertEquals(session.getTimestamp(), OLDEST);

		List<Position> positions = positionRepository.findAllBySessionSessionId(SESSION_ID2);
		assertEquals(positions.size(), 1);
		assertEquals(positions.get(0).getTimestamp(), OLDEST);

	}

	@SuppressWarnings("unchecked")
	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
