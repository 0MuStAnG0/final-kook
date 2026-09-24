package com.example.ui.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.FuchsiaAccent
import com.example.ui.theme.IndigoPrimary
import kotlin.math.roundToInt

// Known Tehran District Centers (Lat, Lng, Name)
data class DistrictMarker(val name: String, val lat: Double, val lng: Double)
data class MapLandmark(val name: String, val lat: Double, val lng: Double, val iconEmoji: String)

val tehranDistricts = listOf(
    DistrictMarker("سعادت‌آباد", 35.7830, 51.3750),
    DistrictMarker("شهرک غرب", 35.7560, 51.3710),
    DistrictMarker("میدان ونک", 35.7570, 51.4100),
    DistrictMarker("جردن / ولیعصر", 35.7700, 51.4180),
    DistrictMarker("تجریش", 35.8050, 51.4280),
    DistrictMarker("اندرزگو", 35.7980, 51.4650),
    DistrictMarker("پاسداران", 35.7620, 51.4640),
    DistrictMarker("ستارخان", 35.7190, 51.3650),
    DistrictMarker("شهرک اکباتان", 35.7080, 51.3090)
)

val tehranLandmarks = listOf(
    MapLandmark("برج میلاد", 35.7448, 51.3753, "🗼"),
    MapLandmark("پل طبیعت", 35.7538, 51.4189, "🌉"),
    MapLandmark("پارک ملت", 35.7760, 51.4120, "🌲"),
    MapLandmark("بوستان پردیسان", 35.7480, 51.3580, "🌿")
)

// Geographic Coordinate Converter
fun geoToScreen(
    lat: Double,
    lng: Double,
    centerLat: Double,
    centerLng: Double,
    centerX: Float,
    centerY: Float,
    zoom: Float
): Offset {
    val scale = 5200f * zoom
    val x = centerX + ((lng - centerLng) * scale).toFloat()
    val y = centerY - ((lat - centerLat) * scale * 1.28f).toFloat()
    return Offset(x, y)
}

@Composable
fun MapCanvas(
    centerLat: Double,
    centerLng: Double,
    userLat: Double,
    userLng: Double,
    mapOffsetX: Float,
    mapOffsetY: Float,
    mapZoom: Float,
    selectedStore: User?,
    pulseScale: Float,
    radarAlpha: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    onTransform: (panX: Float, panY: Float, zoomFactor: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val centerScreenX = canvasWidth / 2f
    val centerScreenY = canvasHeight / 2f
    val mapCenterX = centerScreenX + mapOffsetX
    val mapCenterY = centerScreenY + mapOffsetY

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onTransform(pan.x, pan.y, zoom)
                    }
                }
        ) {
            // 1. Dark Blueprint Background
            drawRect(color = Color(0xFF090D16))

            // 2. Continuous Road Network & Green Zones
            drawContinuousRoadNetwork(
                centerX = mapCenterX,
                centerY = mapCenterY,
                zoom = mapZoom,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight
            )

            // 3. Draw Green Zones (Parks)
            drawTehranParks(
                centerLat = centerLat,
                centerLng = centerLng,
                centerX = mapCenterX,
                centerY = mapCenterY,
                zoom = mapZoom
            )

            // 4. Draw Route from User to Selected Store
            selectedStore?.location?.let { destLoc ->
                val userPt = geoToScreen(
                    lat = userLat,
                    lng = userLng,
                    centerLat = centerLat,
                    centerLng = centerLng,
                    centerX = mapCenterX,
                    centerY = mapCenterY,
                    zoom = mapZoom
                )
                val destPt = geoToScreen(
                    lat = destLoc.latitude,
                    lng = destLoc.longitude,
                    centerLat = centerLat,
                    centerLng = centerLng,
                    centerX = mapCenterX,
                    centerY = mapCenterY,
                    zoom = mapZoom
                )

                drawRoute(userPt, destPt, mapZoom)
            }

            // 5. Draw Animated GPS User Location Marker
            val userScreenPos = geoToScreen(
                lat = userLat,
                lng = userLng,
                centerLat = centerLat,
                centerLng = centerLng,
                centerX = mapCenterX,
                centerY = mapCenterY,
                zoom = mapZoom
            )

            drawUserLocation(userScreenPos, pulseScale, radarAlpha)
        }

        // 6. District Watermark Labels in Persian
        tehranDistricts.forEach { district ->
            val pos = geoToScreen(
                lat = district.lat,
                lng = district.lng,
                centerLat = centerLat,
                centerLng = centerLng,
                centerX = mapCenterX,
                centerY = mapCenterY,
                zoom = mapZoom
            )

            // Only render if within or near the screen bounds
            if (pos.x in -100f..(canvasWidth + 100f) && pos.y in -50f..(canvasHeight + 50f)) {
                Text(
                    text = district.name,
                    color = Color(0xFF94A3B8).copy(alpha = 0.35f),
                    fontSize = (11 * mapZoom.coerceIn(0.85f, 1.4f)).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (pos.x - 40f).roundToInt(),
                            y = (pos.y + 15f).roundToInt()
                        )
                    }
                )
            }
        }

        // 7. Iconic Landmarks (Milad Tower, Tabiat Bridge, Parks)
        tehranLandmarks.forEach { landmark ->
            val pos = geoToScreen(
                lat = landmark.lat,
                lng = landmark.lng,
                centerLat = centerLat,
                centerLng = centerLng,
                centerX = mapCenterX,
                centerY = mapCenterY,
                zoom = mapZoom
            )
            if (pos.x in -60f..(canvasWidth + 60f) && pos.y in -60f..(canvasHeight + 60f)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, Color(0xFF334155)),
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (pos.x - 36f).roundToInt(),
                            y = (pos.y - 14f).roundToInt()
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(landmark.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = landmark.name,
                            color = Color(0xFFE2E8F0),
                            fontSize = (10 * mapZoom.coerceIn(0.85f, 1.25f)).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 8. Scale Indicator at Bottom-Start
        val scaleText = when {
            mapZoom >= 1.7f -> "۱۵۰ متر"
            mapZoom >= 1.2f -> "۳۰۰ متر"
            mapZoom >= 0.85f -> "۵۰۰ متر"
            else -> "۱ کیلومتر"
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.65f),
            border = BorderStroke(0.5.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 100.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(2.dp)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(scaleText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Draw continuous seamless grid lines and arterial highways
private fun DrawScope.drawContinuousRoadNetwork(
    centerX: Float,
    centerY: Float,
    zoom: Float,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val minorRoadColor = Color(0xFF161F30)
    val arterialRoadColor = Color(0xFF1E293B)
    val highwayColor = Color(0xFF2E3D52)
    val expresswayGlow = Color(0xFF3B82F6).copy(alpha = 0.15f)

    // A. Infinite seamless minor road grid
    val gridStep = 100f * zoom
    val startX = (centerX % gridStep) - gridStep
    var x = startX
    while (x <= canvasWidth + gridStep) {
        drawLine(
            color = minorRoadColor,
            start = Offset(x, 0f),
            end = Offset(x, canvasHeight),
            strokeWidth = 1.5f * zoom
        )
        x += gridStep
    }

    val startY = (centerY % gridStep) - gridStep
    var y = startY
    while (y <= canvasHeight + gridStep) {
        drawLine(
            color = minorRoadColor,
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = 1.5f * zoom
        )
        y += gridStep
    }

    // B. Major Highways (Niayesh, Hemmat, Chamran, Modarres, Valiasr)
    // 1. Niayesh / Hashemi Rafsanjani Expressway (East-West)
    val niayeshY = centerY - 140f * zoom
    drawLine(
        color = expresswayGlow,
        start = Offset(0f, niayeshY),
        end = Offset(canvasWidth, niayeshY - 20f * zoom),
        strokeWidth = 12f * zoom,
        cap = StrokeCap.Round
    )
    drawLine(
        color = highwayColor,
        start = Offset(0f, niayeshY),
        end = Offset(canvasWidth, niayeshY - 20f * zoom),
        strokeWidth = 6f * zoom,
        cap = StrokeCap.Round
    )

    // 2. Hemmat Expressway (East-West)
    val hemmatY = centerY + 120f * zoom
    drawLine(
        color = expresswayGlow,
        start = Offset(0f, hemmatY),
        end = Offset(canvasWidth, hemmatY + 30f * zoom),
        strokeWidth = 14f * zoom,
        cap = StrokeCap.Round
    )
    drawLine(
        color = highwayColor,
        start = Offset(0f, hemmatY),
        end = Offset(canvasWidth, hemmatY + 30f * zoom),
        strokeWidth = 7f * zoom,
        cap = StrokeCap.Round
    )

    // 3. Chamran Expressway (North-South)
    val chamranX = centerX - 90f * zoom
    drawLine(
        color = expresswayGlow,
        start = Offset(chamranX - 20f * zoom, 0f),
        end = Offset(chamranX + 40f * zoom, canvasHeight),
        strokeWidth = 14f * zoom,
        cap = StrokeCap.Round
    )
    drawLine(
        color = highwayColor,
        start = Offset(chamranX - 20f * zoom, 0f),
        end = Offset(chamranX + 40f * zoom, canvasHeight),
        strokeWidth = 7f * zoom,
        cap = StrokeCap.Round
    )

    // 4. Modarres Expressway (North-South)
    val modarresX = centerX + 180f * zoom
    drawLine(
        color = highwayColor,
        start = Offset(modarresX + 30f * zoom, 0f),
        end = Offset(modarresX - 20f * zoom, canvasHeight),
        strokeWidth = 6f * zoom,
        cap = StrokeCap.Round
    )

    // 5. Valiasr Historic Street (Long diagonal with emerald tree accent)
    val valiasrTop = Offset(centerX + 120f * zoom, 0f)
    val valiasrBottom = Offset(centerX + 60f * zoom, canvasHeight)
    drawLine(
        color = Color(0xFF10B981).copy(alpha = 0.2f),
        start = valiasrTop,
        end = valiasrBottom,
        strokeWidth = 8f * zoom,
        cap = StrokeCap.Round
    )
    drawLine(
        color = arterialRoadColor,
        start = valiasrTop,
        end = valiasrBottom,
        strokeWidth = 4f * zoom,
        cap = StrokeCap.Round
    )

    // 6. Yadegar-e Emam (West curve)
    val yadegarPath = Path().apply {
        moveTo(centerX - 180f * zoom, 0f)
        cubicTo(
            centerX - 240f * zoom, centerY - 80f * zoom,
            centerX - 220f * zoom, centerY + 180f * zoom,
            centerX - 320f * zoom, canvasHeight
        )
    }
    drawPath(
        path = yadegarPath,
        color = highwayColor,
        style = Stroke(width = 6f * zoom, cap = StrokeCap.Round)
    )
}

// Draw parks and green spaces (Pardisan, Mellat, Goftgoo)
private fun DrawScope.drawTehranParks(
    centerLat: Double,
    centerLng: Double,
    centerX: Float,
    centerY: Float,
    zoom: Float
) {
    val parkColor = Color(0xFF064E3B).copy(alpha = 0.35f)
    val parkBorder = Color(0xFF10B981).copy(alpha = 0.4f)

    // Mellat Park (along Valiasr)
    val mellatCenter = geoToScreen(35.7760, 51.4120, centerLat, centerLng, centerX, centerY, zoom)
    drawRoundRect(
        color = parkColor,
        topLeft = Offset(mellatCenter.x - 30f * zoom, mellatCenter.y - 50f * zoom),
        size = Size(60f * zoom, 100f * zoom),
        cornerRadius = CornerRadius(16f * zoom),
        style = androidx.compose.ui.graphics.drawscope.Fill
    )
    drawRoundRect(
        color = parkBorder,
        topLeft = Offset(mellatCenter.x - 30f * zoom, mellatCenter.y - 50f * zoom),
        size = Size(60f * zoom, 100f * zoom),
        cornerRadius = CornerRadius(16f * zoom),
        style = Stroke(width = 1.5f * zoom)
    )

    // Pardisan Nature Park (large green reserve)
    val pardisanCenter = geoToScreen(35.7480, 51.3580, centerLat, centerLng, centerX, centerY, zoom)
    drawRoundRect(
        color = parkColor,
        topLeft = Offset(pardisanCenter.x - 70f * zoom, pardisanCenter.y - 45f * zoom),
        size = Size(140f * zoom, 90f * zoom),
        cornerRadius = CornerRadius(28f * zoom),
        style = androidx.compose.ui.graphics.drawscope.Fill
    )
    drawRoundRect(
        color = parkBorder,
        topLeft = Offset(pardisanCenter.x - 70f * zoom, pardisanCenter.y - 45f * zoom),
        size = Size(140f * zoom, 90f * zoom),
        cornerRadius = CornerRadius(28f * zoom),
        style = Stroke(width = 1.5f * zoom)
    )
}

// Draw glowing navigation path to store
private fun DrawScope.drawRoute(userPt: Offset, destPt: Offset, zoom: Float) {
    val path = Path().apply {
        moveTo(userPt.x, userPt.y)
        val midX = (userPt.x + destPt.x) / 2f + (if (destPt.x > userPt.x) 35f else -35f) * zoom
        val midY = (userPt.y + destPt.y) / 2f - 25f * zoom
        quadraticTo(midX, midY, destPt.x, destPt.y)
    }

    // Outer glow
    drawPath(
        path = path,
        color = FuchsiaAccent.copy(alpha = 0.28f),
        style = Stroke(width = 14f * zoom, cap = StrokeCap.Round)
    )
    // Core vibrant route line
    drawPath(
        path = path,
        color = IndigoPrimary,
        style = Stroke(
            width = 5f * zoom,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f * zoom, 12f * zoom), 0f),
            cap = StrokeCap.Round
        )
    )
}

// Draw User GPS Location with pulsing radar rings
private fun DrawScope.drawUserLocation(pos: Offset, pulseScale: Float, radarAlpha: Float) {
    // Outer animated radar pulse
    drawCircle(
        color = EmeraldSuccess.copy(alpha = radarAlpha),
        radius = 48f * pulseScale,
        center = pos
    )
    // Secondary halo
    drawCircle(
        color = EmeraldSuccess.copy(alpha = 0.25f),
        radius = 24f,
        center = pos
    )
    // White ring
    drawCircle(
        color = Color.White,
        radius = 10f,
        center = pos
    )
    // Solid emerald core
    drawCircle(
        color = EmeraldSuccess,
        radius = 7f,
        center = pos
    )
}
