package pl.argo.argomobile;

import org.ros.internal.message.RawMessage;
import geometry_msgs.Vector3;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Vector3Implemenation implements Vector3 {
    private double x, y, z;

    public Vector3Implemenation() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3Implemenation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public RawMessage toRawMessage() {
        return null;
    }
}
