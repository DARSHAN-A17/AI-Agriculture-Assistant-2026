package com.agri.assistant.ml

import android.content.Context

/**
 * Knowledge base for plant diseases detected by the CNN model.
 * Maps each PlantVillage class label to disease info, cause, treatment, and prevention.
 */
object DiseaseKnowledgeBase {

    data class DiseaseInfo(
        val plantName: String,
        val diseaseName: String,
        val description: String,
        val cause: String,
        val treatment: String,
        val prevention: String
    )

    private val diseaseMap = mapOf(
        "Apple___Apple_scab" to DiseaseInfo(
            plantName = "Apple",
            diseaseName = "Apple Scab",
            description = "A fungal disease causing dark, scabby lesions on leaves and fruit. Can cause premature leaf drop and reduced fruit quality.",
            cause = "Caused by the fungus Venturia inaequalis. Spreads through wind-borne spores in wet, cool conditions.",
            treatment = "Apply fungicides such as Captan or Mancozeb. Remove and destroy infected leaves. Prune to improve air circulation.",
            prevention = "Plant resistant varieties. Apply preventive fungicides in spring. Keep area clean of fallen debris."
        ),
        "Apple___Black_rot" to DiseaseInfo(
            plantName = "Apple",
            diseaseName = "Black Rot",
            description = "Causes dark brown to black circular lesions on leaves (frogeye leaf spot), rotting fruit, and cankers on limbs.",
            cause = "Caused by the fungus Botryosphaeria obtusa. Spreads from mummified fruits and dead wood.",
            treatment = "Prune out dead and infected wood. Apply Captan or Thiophanate-methyl fungicides. Remove mummified fruit.",
            prevention = "Maintain good sanitation. Remove infected plant material. Apply dormant sprays."
        ),
        "Apple___Cedar_apple_rust" to DiseaseInfo(
            plantName = "Apple",
            diseaseName = "Cedar Apple Rust",
            description = "Bright orange-yellow spots appear on leaves and fruit. The fungus requires both apple and cedar/juniper hosts.",
            cause = "Caused by the fungus Gymnosporangium juniperi-virginianae. Alternates between apple and cedar trees.",
            treatment = "Apply Myclobutanil or Mancozeb fungicides during spring. Remove galls from nearby cedar trees.",
            prevention = "Remove nearby cedar/juniper trees if possible. Plant rust-resistant apple varieties."
        ),
        "Apple___healthy" to DiseaseInfo(
            plantName = "Apple",
            diseaseName = "Healthy",
            description = "The leaf appears healthy with no signs of disease.",
            cause = "N/A — Plant is healthy.",
            treatment = "No treatment needed. Continue regular care.",
            prevention = "Maintain regular watering, fertilization, and pest monitoring."
        ),
        "Blueberry___healthy" to DiseaseInfo(
            plantName = "Blueberry",
            diseaseName = "Healthy",
            description = "The blueberry leaf shows no disease symptoms.",
            cause = "N/A — Plant is healthy.",
            treatment = "No treatment needed.",
            prevention = "Maintain acidic soil pH (4.5-5.5), regular mulching, and proper irrigation."
        ),
        "Cherry_(including_sour)___Powdery_mildew" to DiseaseInfo(
            plantName = "Cherry",
            diseaseName = "Powdery Mildew",
            description = "White powdery patches on leaves, shoots, and occasionally fruit. Leaves may curl and become distorted.",
            cause = "Caused by the fungus Podosphaera clandestina. Thrives in warm, dry conditions with cool nights.",
            treatment = "Apply sulfur-based or potassium bicarbonate fungicides. Use neem oil. Remove severely infected parts.",
            prevention = "Ensure good air circulation. Avoid overhead watering. Plant resistant varieties."
        ),
        "Cherry_(including_sour)___healthy" to DiseaseInfo(
            plantName = "Cherry",
            diseaseName = "Healthy",
            description = "The cherry leaf appears healthy with no signs of disease.",
            cause = "N/A — Plant is healthy.",
            treatment = "No treatment needed.",
            prevention = "Regular pruning, balanced fertilization, and appropriate watering."
        ),
        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot" to DiseaseInfo(
            plantName = "Corn (Maize)",
            diseaseName = "Gray Leaf Spot",
            description = "Rectangular gray-brown lesions on leaves that follow leaf veins. Can cause significant yield loss.",
            cause = "Caused by the fungus Cercospora zeae-maydis. Favored by high humidity and moderate temperatures.",
            treatment = "Apply foliar fungicides (Strobilurin or Triazole). Remove crop residue after harvest.",
            prevention = "Rotate crops. Plant resistant hybrids. Manage crop residue through tillage."
        ),
        "Corn_(maize)___Common_rust_" to DiseaseInfo(
            plantName = "Corn (Maize)",
            diseaseName = "Common Rust",
            description = "Small, circular to elongated brown-red pustules on both leaf surfaces. Can reduce photosynthesis significantly.",
            cause = "Caused by the fungus Puccinia sorghi. Spores spread through wind from southern regions.",
            treatment = "Apply Mancozeb or Propiconazole fungicide if infection is severe. Usually no treatment needed for light cases.",
            prevention = "Plant rust-resistant hybrids. Early planting can help avoid peak spore periods."
        ),
        "Corn_(maize)___Northern_Leaf_Blight" to DiseaseInfo(
            plantName = "Corn (Maize)",
            diseaseName = "Northern Leaf Blight",
            description = "Large, cigar-shaped gray-green lesions on leaves. Severe cases can reduce yields by up to 50%.",
            cause = "Caused by the fungus Exserohilum turcicum. Favored by cool, wet weather.",
            treatment = "Apply Azoxystrobin or Propiconazole fungicides. Time application before tasseling.",
            prevention = "Plant resistant hybrids. Rotate to non-host crops. Manage residue."
        ),
        "Corn_(maize)___healthy" to DiseaseInfo(
            plantName = "Corn (Maize)",
            diseaseName = "Healthy",
            description = "The corn leaf appears healthy.",
            cause = "N/A — Plant is healthy.",
            treatment = "No treatment needed.",
            prevention = "Ensure proper spacing, irrigation, and fertilization."
        ),
        "Grape___Black_rot" to DiseaseInfo(
            plantName = "Grape",
            diseaseName = "Black Rot",
            description = "Brown circular leaf lesions with dark borders. Berries shrivel into hard, black mummies.",
            cause = "Caused by the fungus Guignardia bidwellii. Thrives in warm, humid conditions.",
            treatment = "Apply Myclobutanil or Mancozeb fungicides. Remove and destroy mummified fruit and infected leaves.",
            prevention = "Improve air circulation by proper pruning. Remove mummified berries. Use preventive fungicides."
        ),
        "Grape___Esca_(Black_Measles)" to DiseaseInfo(
            plantName = "Grape",
            diseaseName = "Esca (Black Measles)",
            description = "Interveinal chlorosis and necrosis on leaves giving a tiger-stripe pattern. Dark spots on berries.",
            cause = "Caused by a complex of fungi including Phaeomoniella and Phaeoacremonium species.",
            treatment = "No effective chemical treatment. Remove severely affected vines. Apply wound protectants after pruning.",
            prevention = "Avoid large pruning wounds. Apply pruning wound sealants. Minimize vine stress."
        ),
        "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)" to DiseaseInfo(
            plantName = "Grape",
            diseaseName = "Leaf Blight (Isariopsis Leaf Spot)",
            description = "Irregular brown spots on leaves, often with yellow halos. Can lead to premature defoliation.",
            cause = "Caused by the fungus Pseudocercospora vitis. Favored by wet conditions.",
            treatment = "Apply Bordeaux mixture or copper-based fungicides. Remove infected leaves.",
            prevention = "Ensure adequate spacing and canopy management. Apply preventive fungicides."
        ),
        "Grape___healthy" to DiseaseInfo(
            plantName = "Grape",
            diseaseName = "Healthy",
            description = "The grape leaf is healthy.",
            cause = "N/A",
            treatment = "No treatment needed.",
            prevention = "Regular pruning, proper irrigation, and balanced fertilization."
        ),
        "Orange___Haunglongbing_(Citrus_greening)" to DiseaseInfo(
            plantName = "Orange",
            diseaseName = "Citrus Greening (Huanglongbing)",
            description = "Asymmetric yellowing of leaves (blotchy mottle). Fruit is lopsided, small, and bitter. Most destructive citrus disease.",
            cause = "Caused by the bacterium Candidatus Liberibacter. Spread by the Asian citrus psyllid insect.",
            treatment = "No cure exists. Remove infected trees to prevent spread. Control psyllid populations with insecticides.",
            prevention = "Use disease-free nursery stock. Monitor and control psyllid populations. Remove infected trees early."
        ),
        "Peach___Bacterial_spot" to DiseaseInfo(
            plantName = "Peach",
            diseaseName = "Bacterial Spot",
            description = "Small, dark spots on leaves that may drop out leaving 'shot holes'. Fruit develops pit-like lesions.",
            cause = "Caused by the bacterium Xanthomonas arboricola pv. pruni. Spread by rain splash and wind.",
            treatment = "Apply copper-based bactericides. Use oxytetracycline sprays during bloom.",
            prevention = "Plant resistant varieties. Avoid overhead irrigation. Maintain good tree vigor."
        ),
        "Peach___healthy" to DiseaseInfo(
            plantName = "Peach",
            diseaseName = "Healthy",
            description = "The peach leaf shows no disease symptoms.",
            cause = "N/A",
            treatment = "No treatment needed.",
            prevention = "Regular pruning, pest monitoring, and balanced nutrition."
        ),
        "Pepper,_bell___Bacterial_spot" to DiseaseInfo(
            plantName = "Bell Pepper",
            diseaseName = "Bacterial Spot",
            description = "Water-soaked spots on leaves that turn brown and papery. Can affect fruit causing raised, scab-like lesions.",
            cause = "Caused by Xanthomonas campestris pv. vesicatoria. Spread via contaminated seed and rain splash.",
            treatment = "Apply copper hydroxide + Mancozeb fungicide. Remove infected plants. Use clean seed.",
            prevention = "Use disease-free seed. Rotate crops (2-3 years). Avoid overhead watering."
        ),
        "Pepper,_bell___healthy" to DiseaseInfo(
            plantName = "Bell Pepper",
            diseaseName = "Healthy",
            description = "The pepper leaf is healthy.",
            cause = "N/A",
            treatment = "No treatment needed.",
            prevention = "Regular watering, mulching, and pest monitoring."
        ),
        "Potato___Early_blight" to DiseaseInfo(
            plantName = "Potato",
            diseaseName = "Early Blight",
            description = "Dark brown to black concentric ring (target-like) spots on lower leaves. Progresses upward.",
            cause = "Caused by the fungus Alternaria solani. Favored by warm temperatures and frequent wetting.",
            treatment = "Apply Chlorothalonil or Mancozeb fungicides. Remove infected lower leaves.",
            prevention = "Use certified disease-free seed. Rotate crops. Hill up soil around plants. Ensure proper spacing."
        ),
        "Potato___Late_blight" to DiseaseInfo(
            plantName = "Potato",
            diseaseName = "Late Blight",
            description = "Water-soaked grey-green patches on leaves that quickly turn brown-black. White mold may appear underneath. Caused the Irish Potato Famine.",
            cause = "Caused by the oomycete Phytophthora infestans. Spreads rapidly in cool, wet weather.",
            treatment = "Apply Metalaxyl + Mancozeb or Cymoxanil-based fungicides immediately. Destroy infected plants.",
            prevention = "Use resistant varieties. Avoid overhead irrigation. Apply preventive fungicides. Destroy volunteer plants."
        ),
        "Potato___healthy" to DiseaseInfo(
            plantName = "Potato",
            diseaseName = "Healthy",
            description = "The potato leaf shows no signs of disease.",
            cause = "N/A",
            treatment = "No treatment needed.",
            prevention = "Use certified seed, rotate crops, and maintain proper hilling."
        ),
        "Raspberry___healthy" to DiseaseInfo(
            plantName = "Raspberry",
            diseaseName = "Healthy",
            description = "The raspberry leaf is healthy with no disease signs.",
            cause = "N/A",
            treatment = "No treatment needed.",
            prevention = "Prune old canes, maintain air circulation, and use mulch."
        ),
        "Soybean___healthy" to DiseaseInfo(
            plantName = "Soybean",
            diseaseName = "Healthy",
            description = "The soybean leaf is healthy.",
            cause = "N/A",
            treatment = "No treatment needed.",
            prevention = "Rotate crops and maintain proper drainage."
        ),
        "Squash___Powdery_mildew" to DiseaseInfo(
            plantName = "Squash",
            diseaseName = "Powdery Mildew",
            description = "White powdery coating on leaf surfaces. Severely affected leaves yellow, dry out, and die.",
            cause = "Caused by Podosphaera xanthii or Erysiphe cichoracearum fungi. Thrives in warm, dry conditions.",
            treatment = "Apply sulfur-based fungicides or potassium bicarbonate. Neem oil can also be effective.",
            prevention = "Plant resistant varieties. Ensure good air circulation. Avoid overcrowding."
        ),
        "Strawberry___Leaf_scorch" to DiseaseInfo(
            plantName = "Strawberry",
            diseaseName = "Leaf Scorch",
            description = "Small dark purple spots on upper leaf surface that enlarge and coalesce. Leaves appear scorched.",
            cause = "Caused by the fungus Diplocarpon earlianum. Favored by warm, wet conditions.",
            treatment = "Remove and destroy infected leaves. Apply Captan or Thiram fungicide.",
            prevention = "Plant resistant varieties. Ensure good air circulation. Avoid overhead watering."
        ),
        "Strawberry___healthy" to DiseaseInfo(
            plantName = "Strawberry",
            diseaseName = "Healthy",
            description = "The strawberry leaf is healthy.",
            cause = "N/A",
            treatment = "No treatment needed.",
            prevention = "Maintain good drainage, mulch beds, and remove runners as needed."
        ),
        "Tomato___Bacterial_spot" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Bacterial Spot",
            description = "Small, water-soaked circular spots on leaves that turn brown with yellow halos. Can cause leaf drop.",
            cause = "Caused by Xanthomonas species. Spread by rain, irrigation water, and contaminated tools.",
            treatment = "Apply copper-based bactericides + Mancozeb. Remove infected plants.",
            prevention = "Use disease-free seed. Rotate crops 2-3 years. Avoid working with wet plants."
        ),
        "Tomato___Early_blight" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Early Blight",
            description = "Dark brown spots with concentric rings (target spot) on lower leaves. Progresses upward as plant ages.",
            cause = "Caused by Alternaria solani fungus. Favored by warm, humid conditions and stressed plants.",
            treatment = "Apply Chlorothalonil or Mancozeb fungicide. Remove infected lower leaves. Stake plants.",
            prevention = "Mulch around plants. Water at base. Rotate crops. Use resistant varieties."
        ),
        "Tomato___Late_blight" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Late Blight",
            description = "Large, water-soaked gray-brown patches on leaves. White fuzzy growth may appear on undersides. Fruit develops firm brown rot.",
            cause = "Caused by Phytophthora infestans oomycete. Spreads extremely fast in cool, wet weather.",
            treatment = "Apply Metalaxyl-Mancozeb or Cymoxanil fungicides immediately. Remove and destroy infected plants.",
            prevention = "Use resistant varieties. Ensure good air circulation. Apply preventive fungicides in cool, wet weather."
        ),
        "Tomato___Leaf_Mold" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Leaf Mold",
            description = "Pale green to yellow spots on upper leaf surfaces, with olive-green to grayish-brown velvety mold on undersides.",
            cause = "Caused by the fungus Passalora fulva (formerly Cladosporium fulvum). Thrives in high humidity.",
            treatment = "Apply Mancozeb or chlorothalonil fungicide. Improve ventilation in greenhouses.",
            prevention = "Reduce humidity. Increase air circulation. Use resistant varieties. Avoid wetting foliage."
        ),
        "Tomato___Septoria_leaf_spot" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Septoria Leaf Spot",
            description = "Many small circular spots with dark borders and tan-gray centers on lower leaves. Tiny black dots visible in spots.",
            cause = "Caused by the fungus Septoria lycopersici. Spread via water splash from contaminated soil.",
            treatment = "Apply Chlorothalonil or copper-based fungicides. Remove infected lower leaves.",
            prevention = "Mulch around plants. Avoid overhead watering. Rotate crops. Stake plants upright."
        ),
        "Tomato___Spider_mites Two-spotted_spider_mite" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Spider Mites",
            description = "Tiny yellow spots (stippling) on leaves. Fine webbing visible. Leaves become bronzed and dry.",
            cause = "Caused by Two-spotted Spider Mite (Tetranychus urticae). Thrives in hot, dry, dusty conditions.",
            treatment = "Apply insecticidal soap or neem oil. Use miticides like Abamectin for severe infestations. Spray water to dislodge mites.",
            prevention = "Keep plants well-watered. Avoid dusty conditions. Introduce predatory mites."
        ),
        "Tomato___Target_Spot" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Target Spot",
            description = "Brown spots with concentric rings on leaves, stems, and sometimes fruits. Similar to early blight but caused by different fungus.",
            cause = "Caused by the fungus Corynespora cassiicola. Favored by warm, humid conditions.",
            treatment = "Apply Mancozeb, Chlorothalonil, or copper oxychloride fungicides.",
            prevention = "Improve air circulation. Remove lower leaves. Rotate crops. Use resistant varieties."
        ),
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Yellow Leaf Curl Virus",
            description = "Severe upward curling, cupping, and yellowing of leaves. Stunted growth. Dramatically reduced fruit production.",
            cause = "Caused by Tomato Yellow Leaf Curl Virus (TYLCV). Transmitted by whiteflies (Bemisia tabaci).",
            treatment = "No cure. Remove and destroy infected plants. Control whitefly populations with insecticides or yellow sticky traps.",
            prevention = "Use whitefly-resistant varieties. Install insect-proof nets. Use reflective mulches. Control weeds."
        ),
        "Tomato___Tomato_mosaic_virus" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Tomato Mosaic Virus",
            description = "Mottled light and dark green pattern on leaves. Growth may be stunted. Fruit may be discolored.",
            cause = "Caused by Tomato Mosaic Virus (ToMV). Extremely contagious via contact, tools, and even clothing.",
            treatment = "No cure. Remove and destroy infected plants immediately. Disinfect all tools and hands.",
            prevention = "Use resistant varieties. Disinfect transplanting tools. Wash hands before handling plants. Avoid tobacco near tomatoes."
        ),
        "Tomato___healthy" to DiseaseInfo(
            plantName = "Tomato",
            diseaseName = "Healthy",
            description = "The tomato leaf is healthy with no disease.",
            cause = "N/A",
            treatment = "No treatment needed. Continue regular care.",
            prevention = "Water at base, mulch, rotate crops, and monitor for pests regularly."
        )
    )

    fun getDiseaseInfo(classLabel: String): DiseaseInfo {
        return diseaseMap[classLabel] ?: DiseaseInfo(
            plantName = extractPlantName(classLabel),
            diseaseName = extractDiseaseName(classLabel),
            description = "Disease information is being updated. Please consult a local agriculture expert.",
            cause = "Please consult a local agriculture expert for this specific condition.",
            treatment = "Consult a local agriculture extension office for recommended treatment.",
            prevention = "Practice general good agricultural practices: crop rotation, proper spacing, and hygiene."
        )
    }

    private fun extractPlantName(label: String): String {
        return label.split("___").firstOrNull()?.replace("_", " ") ?: "Unknown Plant"
    }

    private fun extractDiseaseName(label: String): String {
        return label.split("___").getOrNull(1)?.replace("_", " ") ?: "Unknown Disease"
    }

    fun getAllLabels(): List<String> = diseaseMap.keys.toList()
}
