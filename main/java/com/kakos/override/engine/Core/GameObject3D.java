package com.kakos.override.engine.Core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;

public class GameObject3D {
    // Внутренний статический батчер для оптимизации отрисовки всех 3D моделей
    private static final ModelBatch modelBatch = new ModelBatch();
    private static final G3dModelLoader modelLoader = new G3dModelLoader(new JsonReader());
    private static final ObjLoader objLoader = new ObjLoader();

    // --- Основные поля управления ---
    public boolean isVisible = true;

    // Трансформация (Позиция, Поворот, Масштаб)
    public final Vector3 position = new Vector3(0, 0, 0);
    public final Vector3 rotation = new Vector3(0, 0, 0); // Эйлеровы углы (X, Y, Z)
    public final Vector3 scale = new Vector3(1, 1, 1);

    // Ссылки на ресурсы LibGDX
    public Model model;
    public ModelInstance modelInstance;
    public Texture texture;

    // --- Свойства Материала (Управляются напрямую через поля) ---
    public final Color diffuseColor = new Color(Color.WHITE);
    public float shininess = 20.0f; // Блик/Глянец материала
    public float opacity = 1.0f; // Прозрачность

    // --- Настройки UV координат (Смещение и тайлинг текстуры) ---
    public float uvScaleX = 1.0f;
    public float uvScaleY = 1.0f;
    public float uvOffsetX = 0.0f;
    public float uvOffsetY = 0.0f;

    // Вспомогательная матрица для вычисления иерархии
    private final Matrix4 localTransform = new Matrix4();

    /**
     * Загрузка 3D модели из файла (.g3dj или .g3db)
     * 
     * @param internalPath Путь к файлу внутри папки assets (например:
     *                     "models/cube.g3dj")
     */
    public void loadModel(String internalPath) {
        if (model != null)
            model.dispose();

        // Проверяем расширение файла (приводим к нижнему регистру для безопасности)
        if (internalPath.toLowerCase().endsWith(".obj")) {
            // Загружаем как OBJ
            model = objLoader.loadModel(Gdx.files.internal(internalPath));
        } else {
            // Для файлов .g3dj и .g3db используем G3dModelLoader
            model = modelLoader.loadModel(Gdx.files.internal(internalPath));
        }

        modelInstance = new ModelInstance(model);
    }

    /**
     * Загрузка и наложение текстуры на модель
     * 
     * @param internalPath Путь к картинке внутри assets (например:
     *                     "textures/dirt.png")
     */
    public void loadTexture(String internalPath) {
        if (texture != null)
            texture.dispose();

        texture = new Texture(Gdx.files.internal(internalPath));

        if (modelInstance != null) {
            // Заменяем или добавляем текстурный атрибут в дефолтный материал модели
            modelInstance.materials.get(0).set(TextureAttribute.createDiffuse(texture));
        }
    }

    /**
     * Основной метод рендеринга 3D, вызываемый из nrs.render3DScene()
     */
    public void draw3D(Camera camera, Matrix4 parentTransform) {
        if (modelInstance == null)
            return;

        // 1. Обновляем UV координаты и свойства материалов прямо перед отрисовкой
        updateMaterialProperties();

        // 2. Рассчитываем локальную матрицу трансформации из полей
        localTransform.idt()
                .translate(position)
                .rotate(1, 0, 0, rotation.x)
                .rotate(0, 1, 0, rotation.y)
                .rotate(0, 0, 1, rotation.z)
                .scale(scale.x, scale.y, scale.z);

        // 3. Умножаем на родительскую матрицу для поддержки иерархии (если объект
        // вложенный)
        modelInstance.transform.set(parentTransform).mul(localTransform);

        // 4. Отрисовка через ModelBatch
        modelBatch.begin(camera);
        modelBatch.render(modelInstance);
        modelBatch.end();
    }

    /**
     * Метод отрисовки дебаг-информации 3D (вызывается из nrs.render3DScene())
     */
    public void drawDebug3D(ShapeRenderer shapeRenderer, Camera camera, Matrix4 parentTransform) {
        if (modelInstance == null)
            return;

        // Пример дебага: Рисуем оси координат или ограничивающую рамку (Bounding Box) в
        // позиции объекта
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setTransformMatrix(modelInstance.transform);

        // Отрисовка трех осей (X - красный, Y - зеленый, Z - синий) длиной 2 единицы
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 0, 2, 0, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 0, 2, 0);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.line(0, 0, 0, 0, 0, 2);
    }

    /**
     * Синхронизирует публичные поля (цвета, UV, материалы) с внутренними
     * структурами LibGDX
     */
    private void updateMaterialProperties() {
        if (modelInstance == null || modelInstance.materials.size == 0)
            return;

        com.badlogic.gdx.graphics.g3d.Material mat = modelInstance.materials.get(0);

        // Применяем цвета и параметры материала напрямую из полей
        mat.set(ColorAttribute.createDiffuse(diffuseColor));
        mat.set(FloatAttribute.createShininess(shininess));
        mat.set(FloatAttribute.createAlphaTest(opacity));

        // Применяем настройки UV (тайлинг и смещение) к текстурному атрибуту
        if (mat.has(TextureAttribute.Diffuse)) {
            TextureAttribute texAttr = (TextureAttribute) mat.get(TextureAttribute.Diffuse);
            texAttr.scaleU = uvScaleX;
            texAttr.scaleV = uvScaleY;
            texAttr.offsetU = uvOffsetX;
            texAttr.offsetV = uvOffsetY;
        }
    }

    /**
     * Освобождение ресурсов при уничтожении объекта
     */
    public void dispose() {
        if (model != null)
            model.dispose();
        if (texture != null)
            texture.dispose();
    }
}
