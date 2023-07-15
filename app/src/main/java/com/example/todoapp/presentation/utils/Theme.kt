import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.example.todoapp.presentation.utils.LocalMyColors
import com.example.todoapp.presentation.utils.LocalMyTypography
import com.example.todoapp.presentation.utils.blackPalette
import com.example.todoapp.presentation.utils.lightPalette
import com.example.todoapp.presentation.utils.typography


@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when (darkTheme) {
        true -> blackPalette
        else -> lightPalette
    }

    CompositionLocalProvider(
        LocalMyColors provides colors,
        LocalMyTypography provides typography,
        content = content
    )
}
