package com.example.model

data class DistillerGuideItem(
  val id: String,
  val title: String,
  val category: String,
  val summary: String,
  val content: String,
  val tips: List<String>
)

object DistillerGuideRepository {
  val items: List<DistillerGuideItem> = listOf(
    DistillerGuideItem(
      id = "meniscus",
      title = "Hydrometer & Meniscus Technique",
      category = "Measurement",
      summary = "How to accurately read your alcohol hydrometer at eye level.",
      content = """
        An alcohol hydrometer (tralles / proof hydrometer) is designed to float in a clean, narrow glass test jar.
        Because ethanol-water mixtures possess surface tension, liquid creeps up the glass wall and hydrometer stem, forming a curved boundary called a meniscus.
        
        Most precision hydrometers are calibrated for a 'bottom of meniscus' reading. Position your eye strictly level with the true horizontal liquid surface.
        Viewing from above or below introduces parallax error of up to 1-2% ABV.
      """.trimIndent(),
      tips = listOf(
        "Spin the hydrometer gently upon immersion to dislodge adhering air bubbles.",
        "Ensure the hydrometer floats freely without touching the jar wall.",
        "Ensure the spirit is well stirred before sampling to eliminate density layering.",
        "Take temperature reading immediately after hydrometer measurement."
      )
    ),
    DistillerGuideItem(
      id = "contraction",
      title = "Ethanol-Water Volume Contraction",
      category = "Dilution Physics",
      summary = "Why mixing 500 mL water and 500 mL alcohol yields only ~965 mL.",
      content = """
        When ethanol (C2H5OH) and water (H2O) mix, strong intermolecular hydrogen bonds form between the polar hydroxyl groups.
        These molecules pack together much more densely than in their pure separate states.
        
        This physical phenomenon is called volume contraction or volume shrinkage.
        Contraction reaches its maximum at approximately 52% to 55% ABV (around equal molar ratio), resulting in up to 3.6% loss in total liquid volume!
        
        If you rely solely on naive volumetric dilution without contraction compensation, your final spirit will have a higher ABV than desired.
      """.trimIndent(),
      tips = listOf(
        "Always add water to alcohol gradually with steady agitation.",
        "Allow the blend to stabilize and cool; dilution generates an exothermic reaction (heat release).",
        "Perform final hydrometer verification after the mixture has cooled to room temperature.",
        "Use ProofCraft's 'Contraction Correction' switch to get exact legal bottling proof."
      )
    ),
    DistillerGuideItem(
      id = "proof_systems",
      title = "US Proof vs UK Proof vs Gay-Lussac",
      category = "Standards",
      summary = "Comparing global spirit strength measurement scales.",
      content = """
        Historical spirit testing relied on mixing spirit with gunpowder; if the gunpowder still burned, it was '100 proof'.
        
        • US Proof: Exactly 2 × ABV (e.g., 40% ABV = 80° US Proof). 100% pure ethanol is 200° Proof.
        • UK / Imperial Sikes Proof: 100° UK Proof corresponds to 57.1% ABV (or 4/7 by volume). Hence, UK Proof = ABV × 1.7512.
        • Gay-Lussac (°GL): Directly equivalent to percentage alcohol by volume (% ABV) at 15°C or 20°C.
        • Continental European: Standardized on % ABV at 20°C (OIML R22).
      """.trimIndent(),
      tips = listOf(
        "Commercial bottling minimum for whiskey, rum, gin, and vodka is usually 40% ABV (80 US Proof).",
        "Navy Strength gin/rum is traditionally 57% ABV (100° UK Proof).",
        "Bottled-in-Bond spirits in the US must be bottled at exactly 100 US Proof (50% ABV)."
      )
    ),
    DistillerGuideItem(
      id = "water_quality",
      title = "Dilution Water & Avoiding Spirit Haze",
      category = "Finishing",
      summary = "Selecting the right water to prevent flocculation and calcium precipitation.",
      content = """
        Water makes up more than 50% of a finished 80-proof bottle of spirit. Water quality directly governs clarity, mouthfeel, and shelf stability.
        
        Common causes of turbidity / haze:
        1. Mineral Haze: High calcium, magnesium, or carbonate minerals precipitate when alcohol concentration drops below 45% ABV.
        2. Louche / Ouzo Effect: Botanical oils (anethole in absinthe/pastis, terpenes in gin) become insoluble when diluted below critical ABV.
        3. Chill Haze: Long-chain fatty acid ethyl esters (lactic, palmitic esters) precipitate when the bottle is chilled.
      """.trimIndent(),
      tips = listOf(
        "Use Deionized (DI), Reverse Osmosis (RO), or pure Steam-Distilled water for cutting.",
        "Avoid high-mineral tap water or hard spring water.",
        "Chill filtering (cooling spirit to -2°C to 4°C and passing through fine cellulose pads) removes excess fatty acid haze.",
        "Non-chill filtered spirits may louche slightly when served with ice—this is authentic congener flavor!"
      )
    ),
    DistillerGuideItem(
      id = "making_cuts",
      title = "Art & Science of Distillation Cuts",
      category = "Still Run",
      summary = "Sensory and temperature markers for Foreshots, Heads, Hearts, and Tails.",
      content = """
        Fractional distillation separates volatile compounds based on their boiling points and mutual solubilities in ethanol vapor.
        
        1. Foreshots (Boiling ~64-75°C): Methanol, acetone, ethyl acetate. Discard completely (1-2% of total alcohol).
        2. Heads (Boiling ~75-78°C): Acetaldehyde, fruity and solvent aromas, stinging nasal burn. Collect in numbered jars; recycle into feints.
        3. Hearts (Boiling ~78.4-82°C): Sweet, clean ethanol, balanced congeners, rich grain/fruit esters. The prized drinking cut.
        4. Tails (Boiling >82°C): Heavier alcohols (propanol, butanol, isoamyl alcohol / fusel oils), cardboard, damp basement notes. Collect down to ~20% ABV for feints run.
      """.trimIndent(),
      tips = listOf(
        "Never make cuts based purely on thermometer reading or numbers; always use your nose and palate diluted 50/50 with warm water.",
        "Collect into 12 to 24 individual small glass collection jars.",
        "Let the jars air out under coffee filters or paper towels for 24-48 hours before final blending decisions."
      )
    ),
    DistillerGuideItem(
      id = "cask_strengths",
      title = "Cask Entry & Maturation Strengths",
      category = "Aging",
      summary = "Optimal proof for barrel aging whiskey, rum, and brandy.",
      content = """
        Wood extraction dynamics depend heavily on alcohol strength.
        
        Water dissolves wood sugars, hemicellulose, and water-soluble tannins.
        Alcohol dissolves lignin derivatives, vanillin, syringaldehyde, and wood lactones (coconut/oak notes).
        
        • US Legal Maximum Cask Entry: 125 US Proof (62.5% ABV) for Bourbon.
        • Low Cask Entry (50-55% ABV): Sweet, vanilla-forward, rich caramel extraction, softer tannins.
        • High Cask Entry (60-65% ABV): Spicier, more wood lactones and oak tannins, higher volume yield per barrel.
      """.trimIndent(),
      tips = listOf(
        "In dry climates (e.g. Kentucky rackhouse upper floors), water evaporates faster than alcohol, so ABV increases over time in the barrel.",
        "In humid climates (e.g. Scottish dunnage warehouses), alcohol evaporates faster than water, so ABV slowly decreases."
      )
    )
  )
}
