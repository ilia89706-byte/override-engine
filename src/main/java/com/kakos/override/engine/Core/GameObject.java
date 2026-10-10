package com.kakos.override.engine.Core;

import java.io.File;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ArrayMap;

public class GameObject {
    public float x;
    public float y;
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    public float anchorX = 0.5f;
    public float anchorY = 0.5f;
    public float scale = 1.0f;

    public float width;
    public float height;

    public float angle = 0;
    public int layer = 1;

    public Rectangle clipBounds = null;

    public ArrayList childs = new ArrayList<>();
    public ShaderProgram shader;
    public LuaTable data = new LuaTable();
    public LuaTable shaderUniforms = null;

    public LuaTable color;

    public String tag;

    public boolean isVisible = true;

    public ArrayMap<String, LuaFunction> events = new ArrayMap<>();

    public Body body;
    public GameObject parent;

    public GameObject(float x, float y) {
        this.x = x;
        this.y = y;

        color = new LuaTable();
        color.set("r", 1.0f);
        color.set("g", 1.0f);
        color.set("b", 1.0f);
        color.set("a", 1.0f);

        DisplayManager.addToStack(this);
    }

public void changeListener(String key,String value){
LuaTable data = new LuaTable();
data.set("key",key);
data.set("value",value);
events.callEventToObject(this,"changed",data);
}

    public int getLayer() {
        return this.layer;
    }

    public void setColor(float r, float g, float b, float a) {
        color.set("r", r);
        color.set("g", g);
        color.set("b", b);
        color.set("a", a);
    }

    public void setColor(float r, float g, float b) {
        color.set("r", r);
        color.set("g", g);
        color.set("b", b);
    }

    public void setClipBounds(float x, float y, float width, float height) {
        clipBounds = new Rectangle(x, y, width, height);
    }

    public void removeClipBounds() {
        clipBounds = null;
    }

    public GameObject setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        if (this.body != null) {
            this.body.getTransform().setTranslation(x / Physics.SCALE, y / Physics.SCALE);
        }
        return this;
    }

    public void setWidth(float width) {
        this.width = width;
        if (this.body != null) {
            this.body.removeAllFixtures();
            this.body.addFixture(Geometry.createRectangle(
                    this.width / Physics.SCALE,
                    this.height / Physics.SCALE));
        }
    }

    public void setHeight(float height) {
        this.height = height;
        if (this.body != null) {
            this.body.removeAllFixtures();
            this.body.addFixture(Geometry.createRectangle(
                    this.width / Physics.SCALE,
                    this.height / Physics.SCALE));
        }
    }

    public GameObject rotate(float angleOffset) {
        this.angle += angleOffset;
        if (this.body != null) {
            this.body.getTransform().setRotation(Math.toRadians(this.angle));
        }
        return this;
    }

    public void on(String str, LuaFunction callback) {
        events.put(str, callback);
    }

    public void off(String str) {
        events.removeKey(str);
    }

    public void emit(String str, LuaTable data) {
        Events.callEvent(str, data);
    }

    public void insert(GameObject obj) {
        if (obj.parent != null) {
            obj.parent.removeChild(obj);
        } else {
            DisplayManager.gameObjects.remove(obj);
        }

        Matrix4 parentGlobalTransform = this.getGlobalTransform();
        Matrix4 childGlobalTransform = obj.getGlobalTransform();
        Matrix4 localTransform = parentGlobalTransform.cpy().inv().mul(childGlobalTransform);

        Vector3 localPosition = new Vector3();
        localTransform.getTranslation(localPosition);
        obj.x = localPosition.x;
        obj.y = localPosition.y;

        Vector3 localScale = new Vector3();
        localTransform.getScale(localScale);
        obj.scaleX = localScale.x;
        obj.scaleY = localScale.y;

        Quaternion localRotation = new Quaternion();
        localTransform.getRotation(localRotation);
        obj.angle = localRotation.getAngleAround(0, 0, 1);

        if (obj.body != null) {
            obj.body.getTransform().setTranslation(obj.x / Physics.SCALE, obj.y / Physics.SCALE);
            obj.body.getTransform().setRotation(Math.toRadians(obj.angle));
        }

        obj.parent = this;
        childs.add(obj);
    }

    public void remove() {
        Events.callEventToObject(this, "removed", new LuaTable());

        if (this.parent != null) {
            this.parent.removeChild(this);
            this.parent = null;
        } else {
            DisplayManager.gameObjects.remove(this);
        }

        if (this.body != null) {
            Physics.world.removeBody(body);
        }
    }

    public void removeChild(GameObject obj) {
        if (obj != null) {
            childs.remove(obj);
            obj.parent = null;
        }
    }

    public Matrix4 getGlobalTransform() {
        Matrix4 transform = new Matrix4();

        transform.translate(this.x, this.y, 0);

        if (this.angle != 0) {
            transform.rotate(0, 0, 1, this.angle);
        }

        float finalScaleX = this.scaleX * this.scale;
        float finalScaleY = this.scaleY * this.scale;

        if (finalScaleX != 1.0f || finalScaleY != 1.0f) {
            transform.scale(finalScaleX, finalScaleY, 1.0f);
        }

        if (this.parent != null) {
            return this.parent.getGlobalTransform().cpy().mul(transform);
        }

        return transform;
    }

    private GameObject findParent(GameObject target, ArrayList list) {
        for (Object item : list) {
            if (item instanceof GameObject) {
                GameObject obj = (GameObject) item;
                if (obj.childs.contains(target)) {
                    return obj;
                }
                GameObject found = findParent(target, obj.childs);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    public void draw(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {
    }

    public void setShaderUniform(LuaTable shaderUniform) {
        for (int i = 1; i <= shaderUniform.length(); i++) {
            shader.setUniformf(shaderUniform.get(i).get("var").toString(), shaderUniform.get(i).get("value").tofloat());
        }
    }

    public void addShader(String pathToVertex, String pathToFragment) {
        String vertexCode = Gdx.files.absolute(Project.projectRootPath + File.separator + pathToVertex).readString();
        String fragmentCode = Gdx.files.absolute(Project.projectRootPath + File.separator + pathToFragment)
                .readString();

        shader = new ShaderProgram(vertexCode, fragmentCode);
        if (!shader.isCompiled()) {
            Gdx.app.error("ShaderError", shader.getLog());
            Gdx.app.exit();
        }
    }

    public GameObject toFront() {
        this.layer = Integer.MAX_VALUE;
        return this;
    }

    public GameObject toBack() {
        this.layer = Integer.MIN_VALUE;
        return this;
    }

    public void setPhysParams(org.luaj.vm2.LuaTable params) {
        if (this.body == null || params == null) {
            return;
        }

        org.luaj.vm2.LuaValue linearDamping = params.get("linearDamping");
        if (!linearDamping.isnil()) {
            this.body.setLinearDamping(linearDamping.todouble());
        }

        org.luaj.vm2.LuaValue angularDamping = params.get("angularDamping");
        if (!angularDamping.isnil()) {
            this.body.setAngularDamping(angularDamping.todouble());
        }

        org.luaj.vm2.LuaValue gravityScale = params.get("gravityScale");
        if (!gravityScale.isnil()) {
            this.body.setGravityScale(gravityScale.todouble());
        }

        org.luaj.vm2.LuaValue isSensor = params.get("isSensor");

        int fixtureCount = this.body.getFixtureCount();
        if (fixtureCount > 0) {

            org.luaj.vm2.LuaValue density = params.get("density");
            org.luaj.vm2.LuaValue friction = params.get("friction");
            org.luaj.vm2.LuaValue restitution = params.get("restitution");

            for (int i = 0; i < fixtureCount; i++) {
                org.dyn4j.dynamics.BodyFixture fixture = this.body.getFixture(i);

                if (!density.isnil()) {
                    fixture.setDensity(density.todouble());
                }
                if (!friction.isnil()) {
                    fixture.setFriction(friction.todouble());
                }
                if (!restitution.isnil()) {
                    fixture.setRestitution(restitution.todouble());
                }
                if (!isSensor.isnil()) {
                    fixture.setSensor(isSensor.toboolean());
                }
            }

            this.body.updateMass();
        }
    }

    public void setPhysBody(String type, LuaTable fixtureParams) {
        if (this.body == null) {
            this.body = new Body();
        }
        this.body.removeAllFixtures();

        Convex shape = null;

        if (fixtureParams != null && !fixtureParams.isnil()) {
            String shapeType = fixtureParams.get("type").optjstring("rect");

            if (shapeType.equalsIgnoreCase("rect")) {
                double width = fixtureParams.get("width").optdouble(1.0) / Physics.SCALE;
                double height = fixtureParams.get("height").optdouble(1.0) / Physics.SCALE;
                shape = Geometry.createRectangle(width, height);

            } else if (shapeType.equalsIgnoreCase("circle")) {
                double radius = fixtureParams.get("radius").tofloat();
                shape = Geometry.createCircle(radius / Physics.SCALE);

            } else if (shapeType.equalsIgnoreCase("convex")) {

            } else if (shapeType.equalsIgnoreCase("image")) {
                String imagePath = fixtureParams.get("imagePath").optjstring("");
            }
        }

        if (shape == null) {
            shape = Geometry.createRectangle(1.0, 1.0);
        }

        this.body.addFixture(shape);

        if (type.equalsIgnoreCase("dynamic")) {
            this.body.setMass(MassType.NORMAL);
        } else if (type.equalsIgnoreCase("static")) {
            this.body.setMass(MassType.INFINITE);
        }

        if (!Physics.world.containsBody(this.body)) {
            Physics.world.addBody(this.body);
        }

        this.setPosition(this.x, this.y);
        this.body.setUserData(this);
    }

    public GameObject setFixedRotation(boolean fixed) {
        if (this.body != null) {
            this.body.updateMass();
            if (fixed && !this.body.isStatic() && !this.body.isKinematic()) {
                this.body.setMass(org.dyn4j.geometry.MassType.FIXED_ANGULAR_VELOCITY);
            }
        }
        return this;
    }

    public GameObject setRestitution(double restitution) {
        if (this.body != null && this.body.getFixtureCount() > 0) {
            this.body.getFixture(0).setRestitution(restitution);
        }
        return this;
    }

    public GameObject setFriction(double friction) {
        if (this.body != null && this.body.getFixtureCount() > 0) {
            this.body.getFixture(0).setFriction(friction);
        }
        return this;
    }

    public GameObject setSensor(boolean isSensor) {
        if (this.body != null && this.body.getFixtureCount() > 0) {
            this.body.getFixture(0).setSensor(isSensor);
        }
        return this;
    }

    public GameObject applyForce(float fx, float fy) {
        if (this.body != null) {
            this.body.setAtRest(false);
            this.body.applyForce(new org.dyn4j.geometry.Vector2(fx / Physics.SCALE, fy / Physics.SCALE));
        }
        return this;
    }

    public GameObject applyTorque(double torque) {
        if (this.body != null) {
            this.body.setAtRest(false);
            this.body.applyTorque(torque);
        }
        return this;
    }

    public GameObject setAngularVelocity(float degreesPerSec) {
        if (this.body != null) {
            this.body.setAtRest(false);
            this.body.setAngularVelocity(Math.toRadians(degreesPerSec));
        }
        return this;
    }

    public GameObject setLinearDamping(double damping) {
        if (this.body != null) {
            this.body.setLinearDamping(damping);
        }
        return this;
    }

    public GameObject setAngularDamping(double damping) {
        if (this.body != null) {
            this.body.setAngularDamping(damping);
        }
        return this;
    }

    public GameObject setLinearVelocity(float vx, float vy) {
        if (this.body != null) {
            this.body.setAtRest(false);
            this.body.getLinearVelocity().set(vx, vy);
        }
        return this;
    }

    public GameObject applyLinearImpulse(float ix, float iy) {
        if (this.body != null) {
            this.body.setAtRest(false);
            this.body.applyImpulse(new org.dyn4j.geometry.Vector2(ix, iy));
        }
        return this;
    }

    public boolean containsPoint(float pointX, float pointY) {
        return false;
    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        if (this.body == null)
            return;

        nrs.beginShape(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 1);
        for (org.dyn4j.dynamics.BodyFixture fixture : this.body.getFixtures()) {
            org.dyn4j.geometry.Convex convex = fixture.getShape();

            if (convex instanceof org.dyn4j.geometry.Circle) {
                org.dyn4j.geometry.Circle c = (org.dyn4j.geometry.Circle) convex;
                float r = (float) (c.getRadius() * Physics.SCALE);
                float localX = (float) (c.getCenter().x * Physics.SCALE);
                float localY = (float) (c.getCenter().y * Physics.SCALE);

                shapeRenderer.circle(localX, localY, r);

                shapeRenderer.line(localX, localY, localX + r, localY);

            } else if (convex instanceof org.dyn4j.geometry.Polygon) {
                org.dyn4j.geometry.Polygon poly = (org.dyn4j.geometry.Polygon) convex;
                org.dyn4j.geometry.Vector2[] vertices = poly.getVertices();

                for (int i = 0; i < vertices.length; i++) {
                    org.dyn4j.geometry.Vector2 v1 = vertices[i];
                    org.dyn4j.geometry.Vector2 v2 = vertices[(i + 1) % vertices.length];

                    shapeRenderer.line(
                            (float) (v1.x * Physics.SCALE), (float) (v1.y * Physics.SCALE),
                            (float) (v2.x * Physics.SCALE), (float) (v2.y * Physics.SCALE));
                }
            }
        }
    }

}
