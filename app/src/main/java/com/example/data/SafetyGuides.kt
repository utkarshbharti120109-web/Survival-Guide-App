package com.example.data

data class GuideArticle(
    val id: String,
    val title: String,
    val category: String, // "First Aid" or "Survival"
    val iconName: String,
    val shortDescription: String,
    val steps: List<String>,
    val criticalWarnings: String = ""
)

object SafetyGuides {
    val firstAidArticles = listOf(
        GuideArticle(
            id = "cuts_scrapes",
            title = "Cuts & Scrapes Treatment",
            category = "First Aid",
            iconName = "ContentCut",
            shortDescription = "Essential minor wound hygiene to stop bleeding and prevent bacterial infections.",
            steps = listOf(
                "Wash hands thoroughly: Clean your own hands first with soap and water to prevent contaminating the wound.",
                "Apply gentle pressure: Use a clean cloth or sterile bandage to apply firm, steady pressure until the bleeding stops.",
                "Rinse the wound: Hold the cut under clean, cool running tap water for several minutes to wash out dirt and debris.",
                "Clean the surrounding area: Wash the skin around the wound with mild soap, but avoid getting soap directly in the wound to prevent irritation.",
                "Apply antibiotic or petroleum jelly: Spread a thin layer of protective ointment to keep the skin moist and prevent scarring.",
                "Cover with a sterile bandage: Apply a clean adhesive tape or bandage to shield the wound from bacteria. Change it daily."
            ),
            criticalWarnings = "Seek professional care if the wash does not remove deep dirt, if the bleed continues after 10 minutes of direct pressure, or if the cut is from a rusty nail or animal bite."
        ),
        GuideArticle(
            id = "minor_burns",
            title = "Minor Burns Treatment",
            category = "First Aid",
            iconName = "LocalFireDepartment",
            shortDescription = "First-degree and small second-degree burn cooling and soothing procedures.",
            steps = listOf(
                "Cool the burn immediately: Hold the burned zone under cool (not freezing) running tap water for 10 to 15 minutes.",
                "Remove restrictive items: Gently pull off rings, bracelets, or tight shoes before the burn area begins to swell.",
                "Avoid breaking blisters: Intact blisters act as a natural sterile barrier. If they pop, clean gently and apply ointment.",
                "Apply clean light moisturizer: Spread pure aloe vera gel or burn cream to relieve friction. Avoid heavy greases like butter.",
                "Bandage loosely: Wrap the burn area with clean, sterile gauze. Keeping pressure off prevents pain and infection."
            ),
            criticalWarnings = "NEVER use ice, ice-water, butter, or toothpaste on burns. Do not treat third-degree deep white/charred burns or chemical/electrical burns at home; call EMS immediately."
        ),
        GuideArticle(
            id = "nosebleeds",
            title = "Nosebleed Instructions",
            category = "First Aid",
            iconName = "Opacity",
            shortDescription = "Swift physical seating and positioning adjustments to stop nasal capillary bleeds.",
            steps = listOf(
                "Sit upright, lean forward: Tilt your body slightly forward. Leaning back will cause swallowing of blood and vomit.",
                "Pinch your nose: Use your thumb and index finger to firmly pinch together both nostrils (the soft part of the nose) closed.",
                "Breathe through your mouth: Keep pinching continuously for a full 10 to 15 minutes without releasing to check.",
                "Apply cold pack: Place a cold ice compress over the bridge of the nose to facilitate capillary vasoconstriction.",
                "Remain calm and rest: Avoid strenuous activity, bending down, or blowing your nose for at least 2 hours."
            ),
            criticalWarnings = "Go to emergency care if bleeding continues after 20 minutes of steady pinching, or if the nosebleed was caused by a strong blow to the head."
        ),
        GuideArticle(
            id = "sprains_strains",
            title = "Sprain & Strain Care",
            category = "First Aid",
            iconName = "Accessible",
            shortDescription = "The classic R.I.C.E. methodology for managing ligament sprains and muscle strains.",
            steps = listOf(
                "REST: Protect and rest the injured joint or limb. Avoid placing weight on the leg or using the strained muscle.",
                "ICE: Apply cold packs or ice wrapped in a damp towel for 15 to 20 minutes at a time, every 2-3 hours.",
                "COMPRESSION: Wrap the area with an elastic bandage (ace wrap) to limit swelling. Wrap supportively but not too tight.",
                "ELEVATION: Prop the injured area on pillows above the level of your heart whenever possible to drain fluid buildup."
            ),
            criticalWarnings = "Consult a doctor if the person cannot bear any weight on the limb, if the bone alignment looks visibly deformed, or if there is severe sensory numbness."
        ),
        GuideArticle(
            id = "choking_hazard",
            title = "Choking Emergency Guidance",
            category = "First Aid",
            iconName = "Hearing",
            shortDescription = "The Heimlich maneuver and back blows for clear airway obstruction response.",
            steps = listOf(
                "Determine if they can breathe: Ask 'Are you choking?'. If they can speak, cough, or breathe, do not perform thrusts; encourage coughing.",
                "Give 5 back blows: Stand slightly behind. Lean the victim forward and deliver five sharp blows between their shoulder blades with the heel of your hand.",
                "Perform 5 abdominal thrusts (Heimlich): Stand behind, wrap your arms around their waist. Make a fist.",
                "Position your fist: Place the thumb-side of your fist just above the navel. Grasp your fist with your other hand.",
                "Press with rapid upward thrusts: Execute fast inward and upward thrusts into the stomach as if attempting to lift the person.",
                "Alternate sequences: Maintain the cycle of 5 back blows and 5 abdominal thrusts until the object is ejected or the victim gasps."
            ),
            criticalWarnings = "If the choking individual loses consciousness, ease them to the ground, call emergency services, and immediately begin CPR compressions (inspecting mouth for blockages first)."
        ),
        GuideArticle(
            id = "cpr_basics",
            title = "CPR Information (Cardiopulmonary)",
            category = "First Aid",
            iconName = "Favorite",
            shortDescription = "Crucial life-saving rhythmic chest compression loops to maintain blood oxygen flow.",
            steps = listOf(
                "Verify safety and response: Shake the person's shoulder and shout 'Are you okay?'. Check for chest breathing.",
                "Call emergency dispatch: Immediately call 911 or localized rescue. Put the phone on speaker mode next to you.",
                "Place hands center of chest: Place the heel of one hand on the center of the chest. Interlock your other hand on top.",
                "Deliver hard, rapid compressions: Press straight down 2 to 2.4 inches deep. Push at a rate of 100 to 120 compressions per minute.",
                "Allow full chest recoil: Let the chest rise completely between compressions. Do not bounce or lean heavily on the chest.",
                "Provide rescue breathing (if certified): After 30 compressions, tilt the head back, pinch nose, and deliver 2 slow deep breaths. Otherwise, perform continuous chest Compress-Only CPR."
            ),
            criticalWarnings = "Perform compressions continuously with minimum interruption. Stop only if an automated external defibrillator (AED) arrives, emergency help takes over, or the person exhibits signs of life."
        ),
        GuideArticle(
            id = "fracture_care",
            title = "Fracture Awareness & Care",
            category = "First Aid",
            iconName = "Warning",
            shortDescription = "Immediate stabilization of suspected skeletal bone fractures to avoid internal tissues damage.",
            steps = listOf(
                "Stop any active bleeding: Apply pressure to the wound with a sterile bandage, clean cloth, or clean clothing piece.",
                "Immobilize the area: DO NOT try to realign the bone or push back protruding bones. Keep the limb completely static.",
                "Apply a temporary splint: Place rolled-up magazines, wood boards, or cardboard along the limb. Tie securely with cloth strips above and below.",
                "Apply cold compresses: Place ice wrapped in cloth on the area to minimize swelling and alleviate throbbing.",
                "Treat for physical shock: Keep the victim lying flat, elevate feet slightly (unless legs are broken), and cover with an emergency blanket."
            ),
            criticalWarnings = "Seek urgent dispatch if the skin is punctured with exposed bone (open fracture), or if the foot/hand below the injury feels cold to touch or turns bluish tint."
        ),
        GuideArticle(
            id = "heatstroke_dehydrate",
            title = "Heat_Emergency & Dehydration",
            category = "First Aid",
            iconName = "WbSunny",
            shortDescription = "Lowering core temperature and restoring electrolytes safely during hyperthermia crises.",
            steps = listOf(
                "Escape the heat: Move the casualty into an air-conditioned room or onto shaded, breezy grass.",
                "Cool down core temperature: Remove heavy clothes, douse skin with cool water, use fan currents, or place wet sheets over the forehead/neck.",
                "Rehydrate slowly: If the person is conscious and cooperative, give them sips of cool water or an electrolyte sports drink.",
                "Lie flat, raise legs: Keep them resting comfortably to stabilize blood flow and curb dizziness."
            ),
            criticalWarnings = "Heatstroke is a medical emergency. If the victim has dry hot skin, feels highly confused, undergoes seizures, or vomits, call emergency services immediately. DO NOT force an unconscious person to swallow fluid."
        ),
        GuideArticle(
            id = "bites_stings",
            title = "Insect Bite & Sting Guidance",
            category = "First Aid",
            iconName = "BugReport",
            shortDescription = "Scraping of stingers and toxic venom neutralization for localized and systemic reactions.",
            steps = listOf(
                "Remove the stinger immediately: Scrape a fingernail, credit card, or plastic item horizontally across the stinger. Do not squeeze with tweezers.",
                "Wash with soapy water: Wash the bite spot thoroughly with warm water and soap to prevent secondary skin infections.",
                "Reduce localized swelling: Hold a cold ice pack on the sting for 10-15 minutes.",
                "Apply topical soothing relief: Spread a small dab of hydrocortisones, calamine lotion, or baking soda paste."
            ),
            criticalWarnings = "Monitor closely for systemic anaphylactic shock: swelling of lips/throat, extreme breathing wheeze, hives, or faintness. Administer epinephrine (EpiPen) and call emergency rescue immediately."
        )
    )

    val survivalArticles = listOf(
        GuideArticle(
            id = "water_safety",
            title = "Water Safety & Purification",
            category = "Survival",
            iconName = "WaterDrop",
            shortDescription = "Critical guidelines to source, filter, distill, and successfully purify drinkable water in nature.",
            steps = listOf(
                "Look for flowing water: Source from fast-moving streams or springs. Avoid stagnant green ponds and farm runoff ditches.",
                "Build a sediment filter: Pour cloudy water through a clean bandanna, shirt, or grass/charcoal bottle filter to clear particles.",
                "BOIL water (Safest): Bring water to a rolling boil for at least 1 full minute (3 minutes at high altitude over 6,500 ft) to kill pathogens.",
                "Chemical Purifiers: Utilize Chlorine Dioxide tablets, iodine, or standard bleach (8 drops of unscented liquid bleach per gallon). Shake and wait 30 minutes.",
                "Solar Disinfection (SODIS): If no tools exist, place clear plastic bottles filled with water horizontally in direct sunlight for 6 to 8 hours."
            ),
            criticalWarnings = "NEVER drink raw seawater, brackets water, urine, or alcohol for hydration — doing so severely increases kidney failure and fatal dehydration rates."
        ),
        GuideArticle(
            id = "shelter_building",
            title = "Shelter Building Basics",
            category = "Survival",
            iconName = "Home",
            shortDescription = "Constructing thermal barriers to maintain core temperatures and block wind/rain.",
            steps = listOf(
                "Choose safe high ground: Avoid dry stream beds (flash floods), cliff bottoms (rock slides), or under rotting overhead branches.",
                "Insulate from the ground: Lay down a thick 6-to-12-inch mattress of dry leaves, pine needles, or spruce boughs. Ground contact drains heat quickly.",
                "Construct the frame (Lean-To): Support a main ridgepole log against a tree trunk or fork, or build an A-frame structure with heavy limbs.",
                "Layer shingles: Stack branches packed tightly with leaves, moss, pine straw, and bark slabs. Work from the bottom up so rain sheds off.",
                "Keep it compact: Build the shelter only slightly larger than your body. A small, dry space is easier to warm up with body heat."
            ),
            criticalWarnings = "Keep indoor fire pits ventilated with a dedicated smoke hole at the top. Never use wet river rocks inside or directly bordering a fire pit, as trapped steam makes them explode."
        ),
        GuideArticle(
            id = "food_safety",
            title = "Food Safety in Wilderness",
            category = "Survival",
            iconName = "Restaurant",
            shortDescription = "Sourcing edible flora, trapping protocols, and safe outdoor preparation instructions.",
            steps = listOf(
                "Prioritize water over eating: You can survive 3 weeks without food. Do not eat if water supply is low, as digestion burns fluids.",
                "Cook all wild meats: Thoroughly boil or roast insects, frogs, fish, or rodents to destroy parasites and pathogens.",
                "Avoid unknown plants: The 'Universal Edibility Test' takes up to 24 hours. Unless 100% sure, do not ingest wild mushrooms or berries.",
                "Store food high: Hang meat and scavenged foods on a high tree branch (Bear Bag) at least 10 feet off the ground and 4 feet from the trunk.",
                "Maintain camp hygiene: Keep food preparation zones at least 200 feet away from your sleeping shelter area."
            ),
            criticalWarnings = "Avoid all white berries, plants with milky sap, bulbs resembling onions but lacking the scent, and umbrella-shaped flowers which often belong to highly lethal Hemlock families."
        ),
        GuideArticle(
            id = "emergency_signaling",
            title = "Emergency Signaling Methods",
            category = "Survival",
            iconName = "Vibration",
            shortDescription = "How to generate visual, acoustic, and reflective indicators to guide rescue planes.",
            steps = listOf(
                "Use a signaling mirror: Reflect sunlight toward search aircraft. Angle the flash sweep, targeting with a sighted keyhole.",
                "Light three fires: Arrange three fires in a perfect triangle or straight line spaced 100 feet apart, the international distress signal.",
                "Create green smoke: Toss green evergreen boughs, pine needles, or damp leaves onto hot coals to generate heavy, billowy smoke.",
                "Acoustic whistle distress: Sound three loud, long blasts on a whistle. Wait 1 min, and repeat. Sounds carry much further than screams.",
                "Ground visual markers: Stomp out huge 'K' or 'SOS' letters in snow or arrange high-contrast logs/rocks on flat clearings."
            ),
            criticalWarnings = "Do not waste energy screaming loudly if search parties are far. Whistles and visual reflections require minimal physical strain and communicate across miles."
        ),
        GuideArticle(
            id = "navigation_methods",
            title = "Navigation Hacks & Basics",
            category = "Survival",
            iconName = "CompassCalibration",
            shortDescription = "A compass, watch, shadow stick, and celestial orientation guides.",
            steps = listOf(
                "Shadow stick method: Push a stick vertically into flat soil. Mark the tip shadow point. Wait 20 minutes, mark the new tip shadow. Line is West to East.",
                "Watch face compass: Hold an analog watch flat. Point the hour hand at the sun. South is exactly halfway between the hour hand and 12 o'clock.",
                "Find North Star (Polaris): Locate the Ursa Major (Big Dipper) constellation. Track the two stars on the outer edge of the cup; trace a straight line up to find Polaris.",
                "Orient your route: Keep a linear baseline (e.g., walk directly toward a mountain ridge) rather than wandering randomly. Look back often to inspect landmarks."
            ),
            criticalWarnings = "Moss does NOT always grow strictly on the North side of trees. Moss thrives on moisture, which can occur on any side in thick forests. Use celestial markers or a true magnetic compass."
        ),
        GuideArticle(
            id = "outdoor_survival",
            title = "Outdoor Survival Tips",
            category = "Survival",
            iconName = "Trees",
            shortDescription = "Core psychological guidelines, planning models, and heat conservation practices.",
            steps = listOf(
                "S.T.O.P. rule: STOP (sit down), THINK (remain calm), OBSERVE (analyze resources), PLAN (formulate survival steps before moving).",
                "Apply the Rule of Threes: 3 minutes without air, 3 hours without shelter in harsh cold/storms, 3 days without water, 3 weeks without food.",
                "Avoid sweating: Sweating in cold conditions wets your base layer, which rapidly induces hypothermia when cooling. Vent clothing.",
                "Stay with your vehicle/aircraft: A vehicle provides immediate thermal shelter, is highly visible from air circles, and contains fuel/mirrors."
            ),
            criticalWarnings = "The primary killer in remote areas is panic. Conserve your energy, sit down, breathe, and avoid making frantic movements that exhaust oxygen."
        ),
        GuideArticle(
            id = "urban_prep",
            title = "Urban Disaster Preparedness",
            category = "Survival",
            iconName = "LocationCity",
            shortDescription = "Evacuation routes, utility cutoffs, communication structures, and flat shelter systems.",
            steps = listOf(
                "Map building escape lines: Always inspect emergency stairwells and exits when staying in hotels or apartments. Do not use elevators.",
                "Know utility safe shutoffs: Locate and color-code the main valves/breakers for home Gas, Water, and Electricity lines.",
                "Keep cash and paper maps: In total grid failure, ATMs and credit networks collapse. Store small currency notes securely.",
                "Establish offline comms: Set an out-of-state phone contact. Local lines quickly jam, but short text SMS often slips through base receivers.",
                "Emergency water stash: Store bottled water near central areas. Keep plastic/Mylar jugs ready, or instantly fill bathtubs during severe warnings."
            ),
            criticalWarnings = "In an urban fire or chemical disaster, stay low to the floor where the air is breathing-safe and cool. Cover oral linings with a wet t-shirt or cloth."
        )
    )
}
