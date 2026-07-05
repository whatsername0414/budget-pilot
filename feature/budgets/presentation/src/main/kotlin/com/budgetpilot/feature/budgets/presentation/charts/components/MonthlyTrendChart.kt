package com.budgetpilot.feature.budgets.presentation.charts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.money.PesoFormatter
import com.budgetpilot.feature.budgets.presentation.charts.model.MonthlyTrendPointUi
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent
import java.time.YearMonth

private const val GRIDLINE_COUNT = 3
private const val AREA_FILL_ALPHA = 0.14f
private const val PESOS_PER_THOUSAND = 1000.0

/** DESIGN-SPEC.md §7: 6-month trend, line + area fill, 3 faint gridlines, tap for exact values. */
@Composable
fun MonthlyTrendChart(
    points: List<MonthlyTrendPointUi>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val currentPoints by rememberUpdatedState(points)
    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries { series(currentPoints.map { it.total.centavos / 100.0 }) }
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val labels = remember(points) { points.map { it.label } }

    val lineLayer =
        rememberLineCartesianLayer(
            lineProvider =
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(primary)),
                        areaFill =
                            LineCartesianLayer.AreaFill.single(
                                Fill(primary.copy(alpha = AREA_FILL_ALPHA)),
                            ),
                    ),
                ),
        )
    val startAxis =
        VerticalAxis.rememberStart(
            valueFormatter =
                remember { CartesianValueFormatter { _, value, _ -> "₱${(value / PESOS_PER_THOUSAND).toInt()}k" } },
            itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { GRIDLINE_COUNT }) },
            guideline = rememberAxisGuidelineComponent(fill = Fill(outlineVariant)),
        )
    val bottomAxis =
        HorizontalAxis.rememberBottom(
            valueFormatter =
                remember(labels) {
                    CartesianValueFormatter { _, value, _ -> labels.getOrNull(value.toInt()).orEmpty() }
                },
        )
    val marker =
        rememberDefaultCartesianMarker(
            label =
                rememberTextComponent(
                    style = TextStyle(color = onPrimary),
                    padding = Insets(8.dp, 4.dp),
                    background = rememberShapeComponent(fill = Fill(primary), shape = RoundedCornerShape(8.dp)),
                ),
            valueFormatter =
                DefaultCartesianMarker.ValueFormatter { _, targets ->
                    val target = targets.firstOrNull() as? LineCartesianLayerMarkerTarget
                    val y =
                        target
                            ?.points
                            ?.firstOrNull()
                            ?.entry
                            ?.y
                            ?: 0.0
                    PesoFormatter.format(Money.ofCentavos((y * 100).toLong()))
                },
        )

    val summary = remember(points) { buildTrendSummary(points) }
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                lineLayer,
                startAxis = startAxis,
                bottomAxis = bottomAxis,
                marker = marker,
            ),
        modelProducer = modelProducer,
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp)
                .semantics { contentDescription = summary },
    )
}

private fun buildTrendSummary(points: List<MonthlyTrendPointUi>): String {
    val peak = points.maxByOrNull { it.total } ?: return "Monthly spending trend."
    return "Monthly spending trend. Spending peaked in ${peak.label} at ${PesoFormatter.format(peak.total)}."
}

@PreviewLightDark
@Composable
private fun MonthlyTrendChartPreview() {
    BudgetPilotTheme {
        Surface {
            MonthlyTrendChart(
                points =
                    listOf(
                        MonthlyTrendPointUi(YearMonth.now().minusMonths(5), "Feb", Money.fromPesos("12000.00")),
                        MonthlyTrendPointUi(YearMonth.now().minusMonths(4), "Mar", Money.fromPesos("15500.00")),
                        MonthlyTrendPointUi(YearMonth.now().minusMonths(3), "Apr", Money.fromPesos("9800.00")),
                        MonthlyTrendPointUi(YearMonth.now().minusMonths(2), "May", Money.fromPesos("24310.00")),
                        MonthlyTrendPointUi(YearMonth.now().minusMonths(1), "Jun", Money.fromPesos("18200.00")),
                        MonthlyTrendPointUi(YearMonth.now(), "Jul", Money.fromPesos("6100.00")),
                    ),
            )
        }
    }
}
