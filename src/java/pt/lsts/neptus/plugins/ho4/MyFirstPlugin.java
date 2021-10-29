package pt.lsts.neptus.plugins.ho4;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.Vector;

import com.google.common.eventbus.Subscribe;

import org.apache.xpath.objects.XString;
import pt.lsts.imc.IMCAddressResolver;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.PathControlState;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.Rpm;
import pt.lsts.imc.Temperature;
import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.console.ConsoleLayer;
import pt.lsts.neptus.console.events.ConsoleEventMainSystemChange;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.PluginDescription.CATEGORY;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.types.vehicle.VehicleType;
import pt.lsts.neptus.types.vehicle.VehiclesHolder;


@PluginDescription(author="Manuel R.", category=CATEGORY.UNSORTED, name="Myplugin", version = "0.8", description = "This is my first plugin")
public class MyFirstPlugin extends ConsoleLayer {

    private VehicleType vehicle;
    private IMCAddressResolver res = IMCDefinition.getInstance().getResolver();
    private int mainvehicleId;
    private short rpmVal = -1;
    private double tempVal = Double.MAX_VALUE;
    private double end_lon_val = -1;
    private double end_lat_val = -1;
    private String[] plan_list = new String[5]; // 5 should be replaced with the max number of plans possible
    private LocationType loc = null;

    public MyFirstPlugin() {
        NeptusLog.pub().info("This is my first plugin");
    }

    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        super.paint(g, renderer);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(Color.BLACK);
        StringBuilder str0 = new StringBuilder();
        str0.append("Selected Vehicle: ");
        str0.append(getConsole().getMainSystem());
        g2.drawString(str0.toString(), 15, 15);

        StringBuilder str1 = new StringBuilder();
        str1.append("RPM: ");
        if (rpmVal != -1)
            str1.append(rpmVal);
        else
            str1.append("N/A");

        g2.drawString(str1.toString(), 15, 30);

        DecimalFormat df2 = new DecimalFormat("#.##");
        StringBuilder str2 = new StringBuilder();
        str2.append("Temp: ");

        if (tempVal != Double.MAX_VALUE)
            str2.append(df2.format(tempVal));
        else
            str2.append("N/A");

        g2.drawString(str2.toString(), 15, 45);

        // Latitude
        StringBuilder str3 = new StringBuilder();
        str3.append("Target latitude: ");
        if (end_lat_val != -1)
            str3.append(end_lat_val);
        else
            str3.append("N/A");

        g2.drawString(str3.toString(), 15, 60);

        // Longitude
        StringBuilder str4 = new StringBuilder();
        str4.append("Target longitude: ");
        if (end_lon_val != -1)
            str4.append(end_lon_val);
        else
            str4.append("N/A");

        g2.drawString(str4.toString(), 15, 75);

        // Plan ID
        StringBuilder str5 = new StringBuilder();
        str5.append("Plan ID: ");
        for (int i = 0; i<plan_list.length;i++)
            if (plan_list[i] != null)
                str5.append(plan_list[i] + ", ");

        g2.drawString(str5.toString(), 15, 90);

        g2.dispose();
    }

    @Subscribe
    public void mainVehicleChangeNotification(ConsoleEventMainSystemChange evt) {
        rpmVal = -1;
        tempVal = Double.MAX_VALUE;
        end_lon_val = -1;
        end_lat_val = -1;
        System.out.println("Selected vehicle is: " + evt.getCurrent());
        vehicle = VehiclesHolder.getVehicleById(evt.getCurrent());

        mainvehicleId = res.resolve(vehicle.getId());
        System.out.println(vehicle.toString() + " "+ mainvehicleId);
    }

    @Subscribe
    public void on(Rpm rpm) {
        if (rpm.getSrc() == mainvehicleId) {
            rpmVal = rpm.getValue();
            //System.out.println(rpmVal);
        }
    }

    @Subscribe
    public void on(Temperature temp) {
        if (temp.getSrc() == mainvehicleId) {
            tempVal = temp.getValue();
        }
    }

    @Subscribe
    public void on(PathControlState msg) {
        if (msg.getSrc() == mainvehicleId) {
            end_lon_val = Math.toDegrees(msg.getEndLon());
        }
        if (msg.getSrc() == mainvehicleId) {
            end_lat_val = Math.toDegrees(msg.getEndLat());
        }
    }

    @Subscribe
    public void on(PlanDB msg) {
        if (msg.getSrc() == mainvehicleId) {
            IMCMessage plan_arg = msg.getArg();
            try {
                Vector<IMCMessage> msg_list_of_plans = plan_arg.getMessageList("plans_info");
                String plan;
                for (int i = 0; i < plan_list.length; i++) {
                    plan_list[i] = null; // Reset list to null
                    if (i < msg_list_of_plans.size()) {
                        IMCMessage plan_message = msg_list_of_plans.get(i);
                        plan = plan_message.getAsString("plan_id");
                        plan_list[i] = plan;
                        System.out.println("plan added to list: " + plan);
                    }
                }
            } catch (Exception e) {
                System.out.println("Likely no 'plans info' field found");
            }
        }
    }

    @Override
    public boolean userControlsOpacity() {
        return false;
    }

    @Override
    public void initLayer() {

    }

    @Override
    public void cleanLayer() {

    }

}