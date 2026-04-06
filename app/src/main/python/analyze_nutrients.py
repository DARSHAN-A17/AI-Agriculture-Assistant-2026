import pickle
import os
import numpy as np

# Global variables for caching the loaded model to prevent slow reloads
_model_data = None


def _load_model():
    """Loads the pickle model, scaler, and label encoder from the Android assets directory."""
    global _model_data
    if _model_data is not None:
        return _model_data

    # the __file__ variable in Chaquopy points to the python script inside the apk
    # The assets fold is generally equivalent to the parent directory of os.path.dirname(__file__)
    # However, it's safer to just load it directly using the filename if it's bundled in the python path
    
    # Try looking in the standard Chaquopy extraction path or current directory
    possible_paths = [
        "soil_nutrient_model.pkl",
        os.path.join(os.path.dirname(__file__), "soil_nutrient_model.pkl")
    ]

    for path in possible_paths:
        if os.path.exists(path):
            with open(path, "rb") as f:
                _model_data = pickle.load(f)
            return _model_data

    raise FileNotFoundError("Could not find soil_nutrient_model.pkl in the Python path.")


def predict_crop(n, p, k, temperature, humidity, ph, rainfall):
    """
    Takes nutrient and environmental parameters, passes them through the StandardScaler,
    runs the RandomForestClassifier, and decodes the result using the LabelEncoder.
    """
    model_data = _load_model()
    
    model = model_data['model']
    scaler = model_data['scaler']
    le = model_data['le']

    # The model expects an array of shape (1, 7)
    # Features: ['N', 'P', 'K', 'temperature', 'humidity', 'ph', 'rainfall']
    input_features = np.array([[n, p, k, temperature, humidity, ph, rainfall]])
    
    # Scale the input
    input_scaled = scaler.transform(input_features)
    
    # Predict
    prediction_encoded = model.predict(input_scaled)
    
    # Decode
    predicted_crop = le.inverse_transform(prediction_encoded)[0]
    
    return predicted_crop
