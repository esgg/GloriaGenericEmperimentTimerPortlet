package eu.gloria.website.liferay.experiments;

import java.io.IOException;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.User;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.util.bridges.mvc.MVCPortlet;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.experiment.online.OnlineExperimentException;
import eu.gloria.gs.services.experiment.online.OnlineExperimentInterface;
import eu.gloria.gs.services.experiment.online.reservation.ExperimentNotInstantiatedException;
import eu.gloria.gs.services.experiment.online.reservation.NoSuchReservationException;

public class ExperimentsTimer extends MVCPortlet {

	private static Log log = LogFactoryUtil.getLog(ExperimentsTimer.class);

	protected String viewJSP;

	private Integer reservationId = -1;
	
	OnlineExperimentInterface experiment = null;
	

	public void init() throws PortletException {

		viewJSP = getInitParameter("view-jsp");

		super.init();
	}

	public void doView(RenderRequest request, RenderResponse response) {
		
		PortletPreferences prefs = request.getPreferences();
		
		try {
			
			reservationId = Integer.parseInt((String) prefs.getValue(
					"reservationId", "-1"));
			
			GSClientProvider.setHost("saturno.datsi.fi.upm.es");
			
			experiment = GSClientProvider.getOnlineExperimentClient();
			
			include(viewJSP, request, response);
			
		} catch (IOException e) {
			log.error("Error to render portlet:" + e.getMessage());
		} catch (PortletException e) {
			log.error("Error to render portlet:" + e.getMessage());
		}
	}

	protected void include(String path, RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {
		PortletRequestDispatcher portletRequestDispatcher = getPortletContext()
				.getRequestDispatcher(path);
		if (portletRequestDispatcher == null) {
			log.error(path + " is not a valid include");
		} else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		final JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {

			User currentUser = UserLocalServiceUtil.getUserById(Long
					.parseLong(request.getRemoteUser()));
			
			long remainingTime = 0;

			GSClientProvider.setCredentials(currentUser.getEmailAddress(),
					currentUser.getPassword());
			
				if (experiment.anyReservationActiveNow()){
					remainingTime = experiment.getExperimentRuntimeInformation(reservationId).getRemainingTime();	
					log.info("User "+currentUser.getEmailAddress()+"has reserve of "+remainingTime+"seconds");
					remainingTime = remainingTime / 60;
					log.info("User "+currentUser.getEmailAddress()+"has reserve of "+remainingTime+"minutes");
					jsonObject.put("hasReserve", true);
				}else {
					log.info("User "+currentUser.getEmailAddress()+"hasn't reserve");
					remainingTime = 0;
					jsonObject.put("hasReserve", false);
					removePage(request, response);
				}
				jsonObject.put("success", true);
				jsonObject.put("reserve", remainingTime);

		} catch (NumberFormatException e) {
			jsonObject.put("success", false);
			jsonObject.put("error", "error");
			log.error("Wrong user identificator:"+e.getMessage());
			removePage(request, response);
		} catch (PortalException e) {
			jsonObject.put("success", false);
			jsonObject.put("error", "error");
			log.error("Portal exception:"+e.getMessage());
			removePage(request, response);
		} catch (SystemException e) {
			jsonObject.put("success", false);
			log.error("Portal exception:"+e.getMessage());
			jsonObject.put("error", "error");
			removePage(request, response);
		} catch (OnlineExperimentException e) {
			log.error("Error to execute action:" + e.getMessage());
			jsonObject.put("success", false);
			jsonObject.put("error", "error");
			removePage(request, response);
		} catch (ExperimentNotInstantiatedException e) {
			jsonObject.put("success", false);
			log.error("Not exist reservation:"+reservationId+"->"+e.getMessage());
			jsonObject.put("error", "error");
			removePage(request, response);
		} catch (NoSuchReservationException e) {
			jsonObject.put("success", false);
			log.error("Not exist reservation:"+reservationId+"->"+e.getMessage());
			jsonObject.put("error", "error");
			removePage(request, response);
		}

		PrintWriter writer = response.getWriter();
		writer.write(jsonObject.toString());
	}

//	private void removePage(ResourceRequest request, ResourceResponse response) {
//		log.info("Borrando....");
//	}
	
	private void removePage(ResourceRequest request, ResourceResponse response) {

		
		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(com.liferay.portal.kernel.util.WebKeys.THEME_DISPLAY);
		Layout layout = themeDisplay.getLayout();

		log.info("Remove page14 "+ layout.getName()+" with plid:"+layout.getPlid());
		
		
	
		try {

			ServiceContext serviceContext = ServiceContextFactory.getInstance(ExperimentsTimer.class.getName(), request);
					
			LayoutLocalServiceUtil.deleteLayout(layout.getPlid(), serviceContext);
			
			log.info("Remove page3 ");

			
		} catch (SystemException e) {
			log.error("Error to remove layout1:" + e.getMessage());
		}  catch (Exception ex){
			log.error("Error to remove layout2:" + ex.getMessage());
		}

	}

}
