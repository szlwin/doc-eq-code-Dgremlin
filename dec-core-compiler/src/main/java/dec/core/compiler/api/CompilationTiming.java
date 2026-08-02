package dec.core.compiler.api;

import java.util.Objects;
import java.util.Optional;

/**
 * 单次编译阶段的不可变单调时钟计时事实。
 */
public final class CompilationTiming {
    private final TimingPhase phase;
    private final Optional<String> pass;
    private final long elapsedNanos;

    /**
     * 创建计时事实并校验阶段、Pass 名称和非负耗时。
     *
     * @param phase 计时阶段
     * @param pass PASS 阶段的稳定名称，其他阶段必须为空
     * @param elapsedNanos 非负单调纳秒耗时
     */
    public CompilationTiming(
            TimingPhase phase,
            Optional<String> pass,
            long elapsedNanos) {
        this.phase = Objects.requireNonNull(phase, "phase");
        Optional<String> requiredPass = Objects.requireNonNull(pass, "pass");
        if (elapsedNanos < 0L) {
            throw new IllegalArgumentException("elapsedNanos must be >= 0");
        }
        if (phase == TimingPhase.PASS) {
            if (!requiredPass.isPresent() || requiredPass.get().trim().isEmpty()) {
                throw new IllegalArgumentException("PASS timing requires a pass name");
            }
            this.pass = Optional.of(requiredPass.get().trim());
        } else {
            if (requiredPass.isPresent()) {
                throw new IllegalArgumentException(
                        "Only PASS timing may contain a pass name");
            }
            this.pass = Optional.empty();
        }
        this.elapsedNanos = elapsedNanos;
    }

    /**
     * 返回计时阶段。
     */
    public TimingPhase phase() {
        return phase;
    }

    /**
     * 返回 PASS 阶段名称，非 PASS 阶段为空。
     */
    public Optional<String> pass() {
        return pass;
    }

    /**
     * 返回非负单调纳秒耗时。
     */
    public long elapsedNanos() {
        return elapsedNanos;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompilationTiming)) {
            return false;
        }
        CompilationTiming that = (CompilationTiming) other;
        return elapsedNanos == that.elapsedNanos
                && phase == that.phase
                && pass.equals(that.pass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phase, pass, elapsedNanos);
    }

    @Override
    public String toString() {
        return "CompilationTiming{"
                + "phase=" + phase
                + ", pass=" + pass
                + ", elapsedNanos=" + elapsedNanos
                + '}';
    }
}
