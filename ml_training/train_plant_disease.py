"""
Plant Disease Detection Model Training Script
==============================================
Trains a CNN model using MobileNetV2 transfer learning on the PlantVillage dataset.

Dataset: https://data.mendeley.com/datasets/tywbtsjrjv/1
Download the "PlantVillage" dataset (without augmentation) and extract to a folder, then run this script.

Usage:
    python train_plant_disease.py --dataset_path /path/to/PlantVillage --epochs 20 --batch_size 32

Output:
    - plant_disease_model.h5 (Keras model)
    - plant_disease_model.tflite (TensorFlow Lite model)
    - plant_labels.txt (class labels)
    - training_history.png (accuracy/loss plots)
"""

import argparse
import os
import numpy as np
import matplotlib.pyplot as plt
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.preprocessing.image import ImageDataGenerator


def create_model(num_classes, input_shape=(224, 224, 3)):
    """Create CNN model using MobileNetV2 transfer learning."""
    base_model = MobileNetV2(
        weights='imagenet',
        include_top=False,
        input_shape=input_shape
    )

    # Freeze base model layers initially
    base_model.trainable = False

    model = keras.Sequential([
        base_model,
        layers.GlobalAveragePooling2D(),
        layers.BatchNormalization(),
        layers.Dropout(0.3),
        layers.Dense(256, activation='relu'),
        layers.BatchNormalization(),
        layers.Dropout(0.3),
        layers.Dense(num_classes, activation='softmax')
    ])

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=0.001),
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    return model, base_model


def train(args):
    """Main training function."""
    print(f"Loading dataset from: {args.dataset_path}")

    # Data augmentation for training
    train_datagen = ImageDataGenerator(
        rescale=1.0 / 255,
        rotation_range=30,
        width_shift_range=0.2,
        height_shift_range=0.2,
        shear_range=0.2,
        zoom_range=0.2,
        horizontal_flip=True,
        fill_mode='nearest',
        validation_split=0.2
    )

    # Only rescaling for validation
    val_datagen = ImageDataGenerator(
        rescale=1.0 / 255,
        validation_split=0.2
    )

    IMG_SIZE = (224, 224)

    train_generator = train_datagen.flow_from_directory(
        args.dataset_path,
        target_size=IMG_SIZE,
        batch_size=args.batch_size,
        class_mode='categorical',
        subset='training',
        shuffle=True
    )

    val_generator = val_datagen.flow_from_directory(
        args.dataset_path,
        target_size=IMG_SIZE,
        batch_size=args.batch_size,
        class_mode='categorical',
        subset='validation',
        shuffle=False
    )

    num_classes = train_generator.num_classes
    class_names = list(train_generator.class_indices.keys())

    print(f"\nNumber of classes: {num_classes}")
    print(f"Training samples: {train_generator.samples}")
    print(f"Validation samples: {val_generator.samples}")
    print(f"\nClasses: {class_names}")

    # Create model
    model, base_model = create_model(num_classes)
    model.summary()

    # Callbacks
    callbacks = [
        keras.callbacks.EarlyStopping(
            monitor='val_accuracy',
            patience=5,
            restore_best_weights=True
        ),
        keras.callbacks.ReduceLROnPlateau(
            monitor='val_loss',
            factor=0.5,
            patience=3,
            min_lr=1e-7
        ),
        keras.callbacks.ModelCheckpoint(
            'plant_disease_best.h5',
            monitor='val_accuracy',
            save_best_only=True,
            verbose=1
        )
    ]

    # Phase 1: Train with frozen base (feature extraction)
    print("\n=== Phase 1: Feature Extraction (frozen base) ===")
    history1 = model.fit(
        train_generator,
        validation_data=val_generator,
        epochs=args.epochs,
        callbacks=callbacks,
        verbose=1
    )

    # Phase 2: Fine-tune top layers of base model
    print("\n=== Phase 2: Fine-Tuning ===")
    base_model.trainable = True

    # Fine-tune from layer 100 onwards
    for layer in base_model.layers[:100]:
        layer.trainable = False

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=1e-5),
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    history2 = model.fit(
        train_generator,
        validation_data=val_generator,
        epochs=args.fine_tune_epochs,
        callbacks=callbacks,
        verbose=1
    )

    # Save model
    output_dir = args.output_dir
    os.makedirs(output_dir, exist_ok=True)

    model_path = os.path.join(output_dir, 'plant_disease_model.h5')
    model.save(model_path)
    print(f"\nModel saved to: {model_path}")

    # Save labels
    labels_path = os.path.join(output_dir, 'plant_labels.txt')
    with open(labels_path, 'w') as f:
        for name in class_names:
            f.write(name + '\n')
    print(f"Labels saved to: {labels_path}")

    # Convert to TFLite
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    tflite_path = os.path.join(output_dir, 'plant_disease_model.tflite')
    with open(tflite_path, 'wb') as f:
        f.write(tflite_model)
    print(f"TFLite model saved to: {tflite_path}")
    print(f"TFLite model size: {len(tflite_model) / (1024 * 1024):.1f} MB")

    # Plot training history
    plot_history(history1, history2, output_dir)

    # Evaluate
    print("\n=== Final Evaluation ===")
    loss, acc = model.evaluate(val_generator)
    print(f"Validation Accuracy: {acc * 100:.2f}%")
    print(f"Validation Loss: {loss:.4f}")


def plot_history(history1, history2, output_dir):
    """Plot and save training history."""
    acc = history1.history['accuracy'] + history2.history['accuracy']
    val_acc = history1.history['val_accuracy'] + history2.history['val_accuracy']
    loss = history1.history['loss'] + history2.history['loss']
    val_loss = history1.history['val_loss'] + history2.history['val_loss']

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

    ax1.plot(acc, label='Training Accuracy')
    ax1.plot(val_acc, label='Validation Accuracy')
    ax1.set_title('Model Accuracy')
    ax1.set_xlabel('Epoch')
    ax1.set_ylabel('Accuracy')
    ax1.legend()
    ax1.grid(True)

    ax2.plot(loss, label='Training Loss')
    ax2.plot(val_loss, label='Validation Loss')
    ax2.set_title('Model Loss')
    ax2.set_xlabel('Epoch')
    ax2.set_ylabel('Loss')
    ax2.legend()
    ax2.grid(True)

    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'training_history.png'), dpi=150)
    print(f"Training history plot saved.")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Train Plant Disease Detection Model')
    parser.add_argument('--dataset_path', type=str, required=True,
                        help='Path to PlantVillage dataset directory')
    parser.add_argument('--epochs', type=int, default=15,
                        help='Number of training epochs for feature extraction')
    parser.add_argument('--fine_tune_epochs', type=int, default=10,
                        help='Number of fine-tuning epochs')
    parser.add_argument('--batch_size', type=int, default=32,
                        help='Batch size')
    parser.add_argument('--output_dir', type=str, default='./output',
                        help='Directory to save outputs')

    args = parser.parse_args()
    train(args)
