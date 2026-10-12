package com.kakos.override.engine.Core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.dynamics.joint.FrictionJoint;
import org.dyn4j.dynamics.joint.Joint;
import org.dyn4j.dynamics.joint.MotorJoint;
import org.dyn4j.dynamics.joint.PinJoint;
import org.dyn4j.dynamics.joint.PrismaticJoint;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.dynamics.joint.WeldJoint;
import org.dyn4j.dynamics.joint.WheelJoint;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kakos.override.engine.luaAPI.Display;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;


abstract class ovrJoint {
    protected Joint<Body> baseJoint;

    public void remove() {
        if (baseJoint != null) {
            Physics.world.removeJoint(baseJoint);
        }
        Physics.joints.remove(this);
    }

    
}


class ovrRevoluteJoint extends ovrJoint {
    ovrRevoluteJoint(GameObject obj1, GameObject obj2, Vector2 anchor) {
        if (obj1.body == null || obj2.body == null) return;
        baseJoint = new RevoluteJoint<>(obj1.body, obj2.body, anchor);
        Physics.world.addJoint(baseJoint);
    }
}

class ovrWeldJoint extends ovrJoint {
    ovrWeldJoint(GameObject obj1, GameObject obj2, Vector2 anchor) {
        if (obj1.body == null || obj2.body == null) return;
        baseJoint = new WeldJoint<>(obj1.body, obj2.body, anchor);
        Physics.world.addJoint(baseJoint);
    }
}

class ovrDistanceJoint extends ovrJoint {
    ovrDistanceJoint(GameObject obj1, GameObject obj2, Vector2 anchor1, Vector2 anchor2) {
        if (obj1.body == null || obj2.body == null) return;
        baseJoint = new DistanceJoint<>(obj1.body, obj2.body, anchor1, anchor2);
        Physics.world.addJoint(baseJoint);
    }
}

class ovrPrismaticJoint extends ovrJoint {
    ovrPrismaticJoint(GameObject obj1, GameObject obj2, Vector2 anchor, Vector2 axis) {
        if (obj1.body == null || obj2.body == null) return;
        baseJoint = new PrismaticJoint<>(obj1.body, obj2.body, anchor, axis);
        Physics.world.addJoint(baseJoint);
    }
}

class ovrWheelJoint extends ovrJoint {
    ovrWheelJoint(GameObject obj1, GameObject obj2, Vector2 anchor, Vector2 axis) {
        if (obj1.body == null || obj2.body == null) return;
        baseJoint = new WheelJoint<>(obj1.body, obj2.body, anchor, axis);
        Physics.world.addJoint(baseJoint);
    }
}

class ovrFrictionJoint extends ovrJoint {
    ovrFrictionJoint(GameObject obj1, GameObject obj2, Vector2 anchor) {
        if (obj1.body == null || obj2.body == null) return;
        baseJoint = new FrictionJoint<>(obj1.body, obj2.body, anchor);
        Physics.world.addJoint(baseJoint);
    }
}



class ovrMotorJoint extends ovrJoint {
    ovrMotorJoint(GameObject obj1, GameObject obj2) {
        if (obj1.body == null || obj2.body == null) return;
        baseJoint = new MotorJoint<>(obj1.body, obj2.body);
        Physics.world.addJoint(baseJoint);
    }
}

class ovrPinJoint extends ovrJoint {
    ovrPinJoint(GameObject obj1, Vector2 anchor) {
        if (obj1.body == null) return;
        baseJoint = new PinJoint<>(obj1.body, anchor);
        Physics.world.addJoint(baseJoint);
    }
}

public class Physics {
    public static final World<Body> world = new World<>();
    public static float SCALE = 32.0f;
    public static List<ovrJoint> joints = new CopyOnWriteArrayList<>();

    private static float accumulator = 0.0f;
    private static final float STEP_TIME = 1.0f / 60.0f;

    static {
        world.setGravity(new Vector2(0, -9.81));

        world.addContactListener(new ContactListenerAdapter<Body>() {
            @Override
            public void begin(ContactCollisionData<Body> collisionData, Contact contact) {
                Body bodyA = collisionData.getBody1();
                Body bodyB = collisionData.getBody2();

                Object userA = bodyA.getUserData();
                Object userB = bodyB.getUserData();

                if (userA instanceof GameObject && userB instanceof GameObject) {
                    GameObject objA = (GameObject) userA;
                    GameObject objB = (GameObject) userB;
                    
                    
                    sendCollisionEvent(objA, objB, "began", collisionData, contact, 0.0);
                    sendCollisionEvent(objB, objA, "began", collisionData, contact, 0.0);
                }
            }

            
            
        });
    }

    private static void sendCollisionEvent(GameObject receiver, GameObject other, String phase,
            ContactCollisionData<Body> collisionData,
            Contact contact, double impulse) {
        LuaTable data = new LuaTable();
        data.set("phase", phase);

        Vector2 worldPoint = contact.getPoint();
        LuaTable pointTable = new LuaTable();
        pointTable.set(1, LuaValue.valueOf(worldPoint.x * SCALE));
        pointTable.set(2, LuaValue.valueOf(worldPoint.y * SCALE));
        data.set("point", pointTable);

        Vector2 normal = collisionData.getContactConstraint().getNormal();
        LuaTable normalTable = new LuaTable();
        normalTable.set(1, LuaValue.valueOf(normal.x));
        normalTable.set(2, LuaValue.valueOf(normal.y));
        data.set("normal", normalTable);
        data.set("impulse", LuaValue.valueOf(impulse * SCALE));
        data.set("other",Display.processObject(other));

        Events.callEventToObject(receiver, "collision", data);
    }

    private static GameObject extractGameObject(LuaValue arg) {
        if (arg == null || arg.isnil())
            return null;
        if (arg.istable()) {
            LuaValue rawChild = arg.get("__raw_java_object");
            if (rawChild.isuserdata() && rawChild.touserdata() instanceof GameObject) {
                return (GameObject) rawChild.touserdata();
            }
        }
        if (arg.isuserdata() && arg.touserdata() instanceof GameObject) {
            return (GameObject) arg.touserdata();
        }
        return null;
    }

    public static void updatePhys(float dt) {
        
        if (dt > 0.25f)
            dt = 0.25f;

        accumulator += dt;

        while (accumulator >= STEP_TIME) {
            world.update(STEP_TIME);
            accumulator -= STEP_TIME;
        }

        for (GameObject obj : DisplayManager.gameObjects) {
            if (obj.body != null) {
                obj.x = (float) (obj.body.getTransform().getTranslationX() * SCALE);
                obj.y = (float) (obj.body.getTransform().getTranslationY() * SCALE);
                obj.angle = (float) Math.toDegrees(obj.body.getTransform().getRotationAngle());
            }
        }
    }

    private static Vector2 getVectorFromLua(LuaValue table) {
        double x = table.get(1).todouble() / SCALE;
        double y = table.get(2).todouble() / SCALE;
        return new Vector2(x, y);
    }

    

    public static ovrRevoluteJoint revoluteJoint(LuaValue obj1, LuaValue obj2, LuaValue anchor) {
        GameObject g1 = extractGameObject(obj1);
        GameObject g2 = extractGameObject(obj2);
        if (g1 == null || g2 == null)
            return null;

        Vector2 ranchor = getVectorFromLua(anchor);
        ovrRevoluteJoint rj = new ovrRevoluteJoint(g1, g2, ranchor);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

    public static ovrWeldJoint weldJoint(LuaValue obj1, LuaValue obj2, LuaValue anchor) {
        GameObject g1 = extractGameObject(obj1);
        GameObject g2 = extractGameObject(obj2);
        if (g1 == null || g2 == null)
            return null;

        Vector2 ranchor = getVectorFromLua(anchor);
        ovrWeldJoint rj = new ovrWeldJoint(g1, g2, ranchor);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

    public static ovrDistanceJoint distanceJoint(LuaValue obj1, LuaValue obj2, LuaValue anchor1, LuaValue anchor2) {
        GameObject g1 = extractGameObject(obj1);
        GameObject g2 = extractGameObject(obj2);
        if (g1 == null || g2 == null)
            return null;

        Vector2 ranchor1 = getVectorFromLua(anchor1);
        Vector2 ranchor2 = getVectorFromLua(anchor2);
        ovrDistanceJoint rj = new ovrDistanceJoint(g1, g2, ranchor1, ranchor2);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

    public static ovrPrismaticJoint prismaticJoint(LuaValue obj1, LuaValue obj2, LuaValue anchor, LuaValue axis) {
        GameObject g1 = extractGameObject(obj1);
        GameObject g2 = extractGameObject(obj2);
        if (g1 == null || g2 == null)
            return null;

        Vector2 ranchor = getVectorFromLua(anchor);
        Vector2 raxis = new Vector2(axis.get(1).todouble(), axis.get(2).todouble()).getNormalized();
        ovrPrismaticJoint rj = new ovrPrismaticJoint(g1, g2, ranchor, raxis);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

    public static ovrWheelJoint wheelJoint(LuaValue obj1, LuaValue obj2, LuaValue anchor, LuaValue axis) {
        GameObject g1 = extractGameObject(obj1);
        GameObject g2 = extractGameObject(obj2);
        if (g1 == null || g2 == null)
            return null;
        Vector2 ranchor = getVectorFromLua(anchor);
        Vector2 raxis = new Vector2(axis.get(1).todouble(), axis.get(2).todouble()).getNormalized();
        ovrWheelJoint rj = new ovrWheelJoint(g1, g2, ranchor, raxis);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

    public static ovrFrictionJoint frictionJoint(LuaValue obj1, LuaValue obj2, LuaValue anchor) {
        GameObject g1 = extractGameObject(obj1);
        GameObject g2 = extractGameObject(obj2);
        if (g1 == null || g2 == null)
            return null;
        Vector2 ranchor = getVectorFromLua(anchor);
        ovrFrictionJoint rj = new ovrFrictionJoint(g1, g2, ranchor);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

    
    
    public static ovrMotorJoint motorJoint(LuaValue obj1, LuaValue obj2) {
        GameObject g1 = extractGameObject(obj1);
        GameObject g2 = extractGameObject(obj2);
        if (g1 == null || g2 == null)
            return null;
        ovrMotorJoint rj = new ovrMotorJoint(g1, g2);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

    
    
    public static ovrPinJoint pinJoint(LuaValue obj1, LuaValue worldAnchor) {
        GameObject g1 = extractGameObject(obj1);
        if (g1 == null)
            return null;
        Vector2 ranchor = getVectorFromLua(worldAnchor);
        ovrPinJoint rj = new ovrPinJoint(g1, ranchor);
        if (rj.baseJoint != null)
            joints.add(rj);
        return rj;
    }

}