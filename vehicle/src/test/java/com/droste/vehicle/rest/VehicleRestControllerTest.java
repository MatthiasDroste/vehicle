package com.droste.vehicle.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static com.droste.vehicle.rest.SessionRestControllerTest.*;
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
import org.springframework.test.web.servlet.MvcResult;
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
	public void testJPA() {
		Vehicle vehicle = this.vehicleRepository.findOne(VIN);
		List<Session> sessions = vehicle.getSessions();
		assertEquals(1, sessions.size());
		assertEquals(SESSION_ID, sessions.get(0).getSessionId());

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
		MvcResult result = mockMvc.perform(get("/vehicle/" + VIN)).andExpect(status().isOk())
				.andExpect(content().contentType(contentType)).andExpect(jsonPath("$.id", is(VIN)))
				.andExpect(jsonPath("$.sessions[0].sessionId", is(SESSION_ID))).andReturn();
		System.out.println("------------" + result.getResponse().getContentAsString());
		// "sessions":[{"sessionId":"6bc6a660dfef4010ded079865f358e30","timestamp":1519990621975}],"id":"WLQBNAL7EM14E3N"
	}

	@Test
	public void getLastPosition() throws Exception {
		MvcResult result = mockMvc.perform(get("/vehicle/" + VIN + "/position/latest")).andExpect(status().isOk())
				.andExpect(content().contentType(contentType)).andExpect(jsonPath("$.timestamp", is(TIMESTAMP)))
				.andReturn();
		System.out.println("PositionResponse" + result.getResponse().getContentAsString());
	}

	@Test
	public void postNewPosition() throws Exception {
		String content = json(testPosition);
		System.out.println("------------CONTENT--------" + content);
		this.mockMvc.perform(post("/vehicle/" + VIN + "/session/" + SESSION_ID + "/position").contentType(contentType)
				.content(content)).andExpect(status().isCreated());
		Vehicle vehicle = this.vehicleRepository.findOne(VIN);
		assertEquals(vehicle.getId(), VIN);
		assertEquals(vehicle.getSessions().size(), 1);
		assertEquals(vehicle.getSessions().get(0).getSessionId(), SESSION_ID);
		assertEquals(vehicle.getSessions().get(0).getTimestamp(), testPosition.getTimestamp());
		assertEquals(vehicle.getSessions().get(0).getPositions().size(), 1);
		assertEquals(vehicle.getSessions().get(0).getPositions().get(0).getTimestamp(), testPosition.getTimestamp());
	}
	
	@Test
	public void postNewVehicle() throws Exception {
		Position testPosition2 = new Position(OLDEST, 48.1167D, 11.5394D, 291);
		String content = json(testPosition2);
		System.out.println("------------CONTENT--------" + content);
		this.mockMvc.perform(post("/vehicle/" + VIN2+ "/session/" + SESSION_ID2 + "/position").contentType(contentType)
				.content(content)).andExpect(status().isCreated());
		Vehicle vehicle = this.vehicleRepository.findOne(VIN2);
		assertEquals(vehicle.getSessions().size(), 1);
		assertEquals(vehicle.getSessions().get(0).getSessionId(), SESSION_ID2);
		assertEquals(vehicle.getSessions().get(0).getTimestamp(), testPosition.getTimestamp());
		assertEquals(vehicle.getSessions().get(0).getPositions().size(), 1);
		assertEquals(vehicle.getSessions().get(0).getPositions().get(0).getTimestamp(), testPosition.getTimestamp());
	}
	
	@Test
	public void postNewSession() throws Exception {
		String content = json(testPosition);
		System.out.println("------------CONTENT--------" + content);
		this.mockMvc.perform(post("/vehicle/" + VIN + "/session/" + SESSION_ID + "asdf/position").contentType(contentType)
				.content(content)).andExpect(status().isCreated());
		Vehicle vehicle = this.vehicleRepository.findOne(VIN);
		assertEquals(vehicle.getId(), VIN);
		assertEquals(vehicle.getSessions().size(), 1);
		assertEquals(vehicle.getSessions().get(0).getSessionId(), SESSION_ID);
		assertEquals(vehicle.getSessions().get(0).getTimestamp(), testPosition.getTimestamp());
		assertEquals(vehicle.getSessions().get(0).getPositions().size(), 1);
		assertEquals(vehicle.getSessions().get(0).getPositions().get(0).getTimestamp(), testPosition.getTimestamp());
	}

	@SuppressWarnings("unchecked")
	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
