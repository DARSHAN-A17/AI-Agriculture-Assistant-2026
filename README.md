# AI-Agriculture-Assistant-2026

An AI-powered agriculture assistant for crop disease detection and soil analysis using machine learning. Optimized for modern Android devices with real-time detection and offline support.

## 🌟 Features

- **Leaf-Based Disease Detection**: Uses CNN architecture (MobileNetV2) to identify 38 distinct plant disease classes across multiple crops.
- **Soil Type Classification**: Image-based soil analysis for identifying Alluvial, Black, Red, Laterite, and other soil types.
- **Smart Nutrient Analysis**: Random Forest based crop recommendation system using Nitrogen (N), Phosphorus (P), Potassium (K), and pH levels.
- **Multi-language Support**: Fully localized interface in English, Hindi (हिंदी), Kannada (ಕನ್ನಡ), Marathi (มराठी), Tamil (தமிழ்), and Telugu (తెలుగు).
- **Offline ML Performance**: All inference is performed on-device using quantized TensorFlow Lite models for speed and privacy.

## 🛠️ Tech Stack

- **Kotlin**: Core application logic.
- **TensorFlow Lite**: On-device machine learning inference.
- **CameraX API**: Robust camera management for image capture and analysis.
- **Material Design 3**: Modern, responsive UI components.
- **ViewBinding**: Type-safe view interaction.
- **Gradle Kotlin DSL**: Flexible build configuration.

## 📁 Project Structure

- `app/`: Android application source code, resources, and UI layouts.
- `ml_training/`: Jupyter Notebooks and Python scripts for model training.
- `models/`: (Reference) Trained TFLite models for deployment.

## 🚀 Installation & Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/DARSHAN-A17/AI-Agriculture-Assistant-2026.git
   ```
2. **Open in Android Studio**: Use Android Studio Hedgehog (2023.1.1) or later.
3. **Download Models**: After training using the scripts in `ml_training/`, place the resulting `.tflite` files in:
   `app/src/main/assets/`
4. **Build and Run**: Deploy to an Android device (Target API 34).

## 📊 Training Your Own Models

See the [ML Training README](ml_training/README.md) for detailed instructions on training the detection models using Google Colab or local Python environments.

## 🤝 Contributors

Developed by **[DARSHAN-A17]** and the AI Agriculture Assistant Team.

## ⚖️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
