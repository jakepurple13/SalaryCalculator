package com.programmersbox.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(padding)
        ) {
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
                modifier = Modifier.fillMaxWidth()
            )

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
                            numberFormatter.format(it.second.unadjusted),
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            numberFormatter.format(it.second.adjusted),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberField(
    value: Double?,
    onValueChange: (Double?) -> Unit,
    labelText: String,
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value?.toString().orEmpty(),
        onValueChange = { v -> onValueChange(v.toDoubleOrNull()) },
        label = { Text(labelText) },
        prefix = prefix,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
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
) {
    OutlinedTextField(
        value?.toString().orEmpty(),
        onValueChange = { v -> onValueChange(v.toIntOrNull()) },
        prefix = prefix,
        label = { Text(labelText) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier
    )
}

class SalaryData {
    var perAmount by mutableStateOf(PerAmount.Hourly)
    var amount by mutableStateOf<Double?>(50.0)
    var hoursPerWeek by mutableStateOf<Double?>(40.0)
    var daysPerWeek by mutableStateOf<Int?>(5)
    var holidaysPerYear by mutableStateOf<Int?>(10)
    var vacationDaysPerYear by mutableStateOf<Int?>(15)

    val amounts by derivedStateOf {
        runCatching {
            val amount = requireNotNull(amount)
            val hoursPerWeek = requireNotNull(hoursPerWeek)
            val daysPerWeek = requireNotNull(daysPerWeek)
            val holidaysPerYear = requireNotNull(holidaysPerYear)
            val vacationDaysPerYear = requireNotNull(vacationDaysPerYear)

            val offDays = holidaysPerYear + vacationDaysPerYear
            when (perAmount) {
                PerAmount.Hourly -> {
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = amount
                        ),
                        daily = Adjustments(
                            unadjusted = amount * (hoursPerWeek / daysPerWeek)
                        ),
                        weekly = Adjustments(
                            unadjusted = amount * hoursPerWeek
                        ),
                        biWeekly = Adjustments(
                            unadjusted = amount * hoursPerWeek * 2
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount * hoursPerWeek * 52 / 24
                        ),
                        monthly = Adjustments(
                            unadjusted = amount * hoursPerWeek * 52 / 12,
                        ),
                        quarterly = Adjustments(
                            unadjusted = amount * hoursPerWeek * (52 / 4),
                        ),
                        yearly = Adjustments(
                            unadjusted = amount * hoursPerWeek * 52,
                            adjusted = amount * hoursPerWeek * (52 - offDays / 5)
                        )
                    )
                }

                PerAmount.Daily -> {
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = amount / (hoursPerWeek / daysPerWeek)
                        ),
                        daily = Adjustments(
                            unadjusted = amount
                        ),
                        weekly = Adjustments(
                            unadjusted = amount * daysPerWeek
                        ),
                        biWeekly = Adjustments(
                            unadjusted = amount * daysPerWeek * 2
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount * daysPerWeek * 52 / 24
                        ),
                        monthly = Adjustments(
                            unadjusted = amount * daysPerWeek * 52 / 12,
                        ),
                        quarterly = Adjustments(
                            unadjusted = amount * daysPerWeek * (52 / 4),
                        ),
                        yearly = Adjustments(
                            unadjusted = amount * daysPerWeek * 52,
                            adjusted = amount * daysPerWeek * (52 - offDays / 5)
                        )
                    )
                }

                PerAmount.Weekly -> {
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = amount / hoursPerWeek
                        ),
                        daily = Adjustments(
                            unadjusted = amount / daysPerWeek
                        ),
                        weekly = Adjustments(
                            unadjusted = amount
                        ),
                        biWeekly = Adjustments(
                            unadjusted = amount * 2
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount * 52 / 24
                        ),
                        monthly = Adjustments(
                            unadjusted = amount * 52 / 12,
                        ),
                        quarterly = Adjustments(
                            unadjusted = amount * (52 / 4),
                        ),
                        yearly = Adjustments(
                            unadjusted = amount * 52,
                            adjusted = amount * (52 - offDays / 5)
                        )
                    )
                }

                PerAmount.BiWeekly -> {
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = amount / 2 / hoursPerWeek
                        ),
                        daily = Adjustments(
                            unadjusted = amount / 2 / daysPerWeek
                        ),
                        weekly = Adjustments(
                            unadjusted = amount / 2
                        ),
                        biWeekly = Adjustments(
                            unadjusted = amount
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount / 2 * 52 / 24
                        ),
                        monthly = Adjustments(
                            unadjusted = amount * 52 / 24,
                        ),
                        quarterly = Adjustments(
                            unadjusted = amount / 2 * (52 / 4),
                        ),
                        yearly = Adjustments(
                            unadjusted = amount / 2 * 52,
                            adjusted = amount / 2 * (52 - offDays / 5)
                        )
                    )
                }

                PerAmount.SemiMonthly -> {
                    val hourly = amount / (hoursPerWeek * 52 / 24)
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = hourly
                        ),
                        daily = Adjustments(
                            unadjusted = hourly * (hoursPerWeek / daysPerWeek)
                        ),
                        weekly = Adjustments(
                            unadjusted = hourly * hoursPerWeek
                        ),
                        biWeekly = Adjustments(
                            unadjusted = hourly * hoursPerWeek * 2
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount
                        ),
                        monthly = Adjustments(
                            unadjusted = amount * 2,
                        ),
                        quarterly = Adjustments(
                            unadjusted = hourly * hoursPerWeek * (52 / 4),
                        ),
                        yearly = Adjustments(
                            unadjusted = hourly * hoursPerWeek * 52,
                            adjusted = hourly * hoursPerWeek * (52 - offDays / 5)
                        )
                    )
                }

                PerAmount.Monthly -> {
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = amount / hoursPerWeek * 52 / 12
                        ),
                        daily = Adjustments(
                            unadjusted = amount / 4 * (hoursPerWeek / daysPerWeek)
                        ),
                        weekly = Adjustments(
                            unadjusted = amount / 4 * hoursPerWeek
                        ),
                        biWeekly = Adjustments(
                            unadjusted = amount / 2 * hoursPerWeek * 2
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount / 2 * hoursPerWeek * 52 / 12
                        ),
                        monthly = Adjustments(
                            unadjusted = amount,
                        ),
                        quarterly = Adjustments(
                            unadjusted = amount * hoursPerWeek * (52 / 4),
                        ),
                        yearly = Adjustments(
                            unadjusted = amount * hoursPerWeek * 52,
                            adjusted = amount * hoursPerWeek * (52 - offDays / 5)
                        )
                    )
                }

                PerAmount.Quarterly -> {
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = amount * 4 / 52 / hoursPerWeek
                        ),
                        daily = Adjustments(
                            unadjusted = amount * 4 / 260
                        ),
                        weekly = Adjustments(
                            unadjusted = amount * 4 / 52,
                        ),
                        biWeekly = Adjustments(
                            unadjusted = amount * 4 / 26,
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount * 4 / 24
                        ),
                        monthly = Adjustments(
                            unadjusted = amount * 4 / 12,
                        ),
                        quarterly = Adjustments(
                            unadjusted = amount,
                        ),
                        yearly = Adjustments(
                            unadjusted = amount * 4,
                        )
                    )
                }

                PerAmount.Yearly -> {
                    SalaryResults(
                        hourly = Adjustments(
                            unadjusted = amount / 52 / hoursPerWeek
                        ),
                        daily = Adjustments(
                            unadjusted = amount / 260
                        ),
                        weekly = Adjustments(
                            unadjusted = amount / 52,
                        ),
                        biWeekly = Adjustments(
                            unadjusted = amount / 26,
                        ),
                        semiMonthly = Adjustments(
                            unadjusted = amount / 24
                        ),
                        monthly = Adjustments(
                            unadjusted = amount / 12,
                        ),
                        quarterly = Adjustments(
                            unadjusted = amount / 4,
                        ),
                        yearly = Adjustments(
                            unadjusted = amount,
                            adjusted = amount / 52 / hoursPerWeek * hoursPerWeek * (52 - offDays / 5)
                        )
                    )
                }
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