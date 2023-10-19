package com.programmersbox.common

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat

@Composable
internal fun App() {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SalaryUI()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SalaryUI() {
    val salaryData = remember { SalaryData() }

    val numberFormatter = remember { NumberFormat.getCurrencyInstance() }

    var showPerAmount by remember { mutableStateOf(false) }

    if (showPerAmount) {
        ModalBottomSheet(
            onDismissRequest = { showPerAmount = false }
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                PerAmount.entries.forEach {
                    ElevatedFilterChip(
                        onClick = {
                            salaryData.perAmount = it
                            showPerAmount = false
                        },
                        label = { Text(it.name) },
                        selected = salaryData.perAmount == it
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salary Calculator") }
            )
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                item {}
                item { Text("Unadjusted", textAlign = TextAlign.Center) }
                item { Text("Adjusted", textAlign = TextAlign.Center) }
                salaryData.amounts.infoMap().forEach {
                    item {
                        Text(
                            it.first.name,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            numberFormatter.format(
                                animateValueAsState(
                                    it.second.unadjusted,
                                    DoubleConverter
                                ).value
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            numberFormatter.format(
                                animateValueAsState(
                                    it.second.adjusted,
                                    DoubleConverter
                                ).value
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NumberField(
                    salaryData.amount,
                    onValueChange = { salaryData.amount = it },
                    labelText = "Amount",
                    prefix = { Text("$") }
                )

                Spacer(Modifier.width(20.dp))

                AssistChip(
                    onClick = { showPerAmount = true },
                    label = { Text(salaryData.perAmount.name) }
                )
            }

            NumberField(
                salaryData.hoursPerWeek,
                onValueChange = { salaryData.hoursPerWeek = it },
                labelText = "Hours per Week",
                modifier = Modifier.fillMaxWidth()
            )

            NumberField(
                salaryData.daysPerWeek,
                onValueChange = { salaryData.daysPerWeek = it },
                labelText = "Days per Week",
                modifier = Modifier.fillMaxWidth()
            )

            NumberField(
                salaryData.holidaysPerYear,
                onValueChange = { salaryData.holidaysPerYear = it },
                labelText = "Holidays per Year",
                modifier = Modifier.fillMaxWidth()
            )

            NumberField(
                salaryData.vacationDaysPerYear,
                onValueChange = { salaryData.vacationDaysPerYear = it },
                labelText = "Vacation Days per Year",
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private val DoubleConverter =
    TwoWayConverter<Double, AnimationVector1D>({ AnimationVector1D(it.toFloat()) }, { it.value.toDouble() })

@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        prefix = prefix,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun NumberField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    labelText: String,
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value?.toString().orEmpty(),
        onValueChange = { v -> onValueChange(v.toIntOrNull()) },
        prefix = prefix,
        label = { Text(labelText) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        singleLine = true,
        modifier = modifier
    )
}

class SalaryData {
    var perAmount by mutableStateOf(PerAmount.Hourly)
    var amount by mutableStateOf("50")
    var hoursPerWeek by mutableStateOf("40")
    var daysPerWeek by mutableStateOf<Int?>(5)
    var holidaysPerYear by mutableStateOf<Int?>(10)
    var vacationDaysPerYear by mutableStateOf<Int?>(15)

    val amounts by derivedStateOf {
        runCatching {
            val amount = requireNotNull(amount.toDoubleOrNull())
            val hoursPerWeek = requireNotNull(hoursPerWeek.toDoubleOrNull())
            val daysPerWeek = requireNotNull(daysPerWeek)
            val holidaysPerYear = requireNotNull(holidaysPerYear)
            val vacationDaysPerYear = requireNotNull(vacationDaysPerYear)

            val offDays = holidaysPerYear + vacationDaysPerYear
            val weeksPerYear = 52.0
            val monthsPerYear = 12.0
            val quartersPerYear = 4.0

            val weeklyHours = hoursPerWeek
            val dailyHours = hoursPerWeek / daysPerWeek
            val monthlyHours = weeklyHours * weeksPerYear / monthsPerYear
            val quarterlyHours = weeklyHours * weeksPerYear / quartersPerYear

            fun getSalaryResults(hourlyAmount: Double): SalaryResults {
                val adjustedYear = hourlyAmount * weeklyHours * (weeksPerYear - offDays / 5)
                return SalaryResults(
                    hourly = Adjustments(
                        unadjusted = hourlyAmount,
                        //TODO: Work on this
                        adjusted = adjustedYear / (weeksPerYear - offDays * 5)
                    ),
                    daily = Adjustments(
                        unadjusted = hourlyAmount * dailyHours,
                        adjusted = adjustedYear / 260
                    ),
                    weekly = Adjustments(
                        unadjusted = hourlyAmount * weeklyHours,
                        adjusted = adjustedYear / weeksPerYear
                    ),
                    biWeekly = Adjustments(
                        unadjusted = hourlyAmount * weeklyHours * 2,
                        adjusted = adjustedYear / weeksPerYear * 2
                    ),
                    semiMonthly = Adjustments(
                        unadjusted = hourlyAmount * monthlyHours * monthsPerYear / 24,
                        adjusted = adjustedYear / 24
                    ),
                    monthly = Adjustments(
                        unadjusted = hourlyAmount * monthlyHours,
                        adjusted = adjustedYear / monthsPerYear
                    ),
                    quarterly = Adjustments(
                        unadjusted = hourlyAmount * quarterlyHours,
                        adjusted = adjustedYear / quartersPerYear
                    ),
                    yearly = Adjustments(
                        unadjusted = hourlyAmount * weeklyHours * weeksPerYear,
                        adjusted = adjustedYear
                    )
                )
            }

            when (perAmount) {
                PerAmount.Hourly -> getSalaryResults(amount)

                PerAmount.Daily -> getSalaryResults(amount / dailyHours)

                PerAmount.Weekly -> getSalaryResults(amount / weeklyHours)

                PerAmount.BiWeekly -> getSalaryResults(amount / 2 / weeklyHours)

                PerAmount.SemiMonthly -> {
                    val hourly = amount / monthlyHours * monthsPerYear / 24
                    getSalaryResults(hourly)
                }

                PerAmount.Monthly -> getSalaryResults(amount / monthlyHours)

                PerAmount.Quarterly -> getSalaryResults(amount / quarterlyHours)

                PerAmount.Yearly -> getSalaryResults(amount / weeklyHours / weeksPerYear)
            }
        }
            .getOrDefault(
                SalaryResults(
                    hourly = Adjustments(0.0),
                    daily = Adjustments(0.0),
                    weekly = Adjustments(0.0),
                    biWeekly = Adjustments(0.0),
                    semiMonthly = Adjustments(0.0),
                    monthly = Adjustments(0.0),
                    quarterly = Adjustments(0.0),
                    yearly = Adjustments(0.0),
                )
            )
    }
}

/**
 * Represents the salary results for different time periods.
 *
 * @property hourly The adjustments for hourly salary.
 * @property daily The adjustments for daily salary.
 * @property weekly The adjustments for weekly salary.
 * @property biWeekly The adjustments for bi-weekly salary.
 * @property semiMonthly The adjustments for semi-monthly salary.
 * @property monthly The adjustments for monthly salary.
 * @property quarterly The adjustments for quarterly salary.
 * @property yearly The adjustments for yearly salary.
 */
data class SalaryResults(
    val hourly: Adjustments,
    val daily: Adjustments,
    val weekly: Adjustments,
    val biWeekly: Adjustments,
    val semiMonthly: Adjustments,
    val monthly: Adjustments,
    val quarterly: Adjustments,
    val yearly: Adjustments,
) {
    fun infoMap() = PerAmount
        .entries
        .map {
            it to when (it) {
                PerAmount.Hourly -> hourly
                PerAmount.Daily -> daily
                PerAmount.Weekly -> weekly
                PerAmount.BiWeekly -> biWeekly
                PerAmount.SemiMonthly -> semiMonthly
                PerAmount.Monthly -> monthly
                PerAmount.Quarterly -> quarterly
                PerAmount.Yearly -> yearly
            }
        }
}

data class Adjustments(
    val unadjusted: Double,
    val adjusted: Double = unadjusted,
)

enum class PerAmount {
    Hourly,
    Daily,
    Weekly,
    BiWeekly,
    SemiMonthly,
    Monthly,
    Quarterly,
    Yearly
}