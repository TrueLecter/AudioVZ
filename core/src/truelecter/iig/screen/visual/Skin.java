package truelecter.iig.screen.visual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import truelecter.iig.Main;
import truelecter.iig.util.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;

public class Skin {
	public class SkinPart {
		private Sprite toRenderer;
		private float x;
		private float y;

		public SkinPart(FileHandle texturePath, float x, float y) {
			toRenderer = new Sprite(new Texture(texturePath));
			this.x = x;
			this.y = y;
		}

		public SkinPart(String texturePath, float x, float y) {
			toRenderer = new Sprite(new Texture(texturePath));
			this.x = x;
			this.y = y;
		}

		public SkinPart(Texture texturePath, float x, float y) {
			toRenderer = new Sprite(texturePath);
			this.x = x;
			this.y = y;
		}

		public void renderer(SpriteBatch sb) {
			toRenderer.setX(x);
			toRenderer.setY(y);
			toRenderer.draw(sb);
		}

		public void setLocation(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private enum SkinPartType {
		CUSTOM, BUTTON, BACKGROUND, TIMEPANEL;

		protected static SkinPartType getTypeFor(String name) {
			String c = name.toLowerCase();
			if (c.equals("button")) {
				return BUTTON;
			}
			if (c.equals("background")) {
				return BACKGROUND;
			}
			if (c.equals("timepanel")) {
				return TIMEPANEL;
			}
			return CUSTOM;
		}
	}

	public Texture play;
	public Texture pause;
	public Texture background;
	public Vector2 timeLeft;
	public Vector2 timePassed;
	public Vector2 soundPos;
	public Vector2 songNamePos;
	public Vector3 timeBar;
	public Texture bars;
	public boolean useOldBars;
	public ArrayList<SkinPart> customPartsBackground;
	public ArrayList<SkinPart> customPartsForeground;

	private static Skin defaultSkin;

	public Skin(File skin) throws InvalidFileFormatException, IOException {
		this(skin, false);
	}

	private Skin(File skin, boolean useInternal) throws InvalidFileFormatException, IOException {
		System.out.println("Using" + (useInternal ? " internal " : " ") + "skin: " + skin.getAbsolutePath());
		Ini ini = new Ini(skin);
		customPartsBackground = new ArrayList<SkinPart>();
		customPartsForeground = new ArrayList<SkinPart>();
		for (String name : ini.keySet()) {
			Ini.Section section = ini.get(name);
			String x = "";
			String y = "";
			String w = "";
			switch (SkinPartType.getTypeFor(name)) {
			case BUTTON:
				x = section.get("playPath", "icon/play.png");
				play = new Texture(Util.getFile(skin, x, useInternal));
				x = section.get("pausePath", "icon/pause.png");
				pause = new Texture(Util.getFile(skin, x, useInternal));
				break;
			case BACKGROUND:
				x = section.get("path", "backgroundV.png");
				background = new Texture(Util.getFile(skin, x, useInternal));
				break;
			case TIMEPANEL:
				x = section.get("timePassedX", "40");
				y = section.get("timePassedY", "100");
				timePassed = new Vector2(parse(x), parse(y));
				x = section.get("timeLeftX", "width - 120");
				y = section.get("timeLeftY", "100");
				timeLeft = new Vector2(parse(x), parse(y));
				x = section.get("songNameX", "50");
				y = section.get("songNameY", "height - 50");
				songNamePos = new Vector2(parse(x), parse(y));
				x = section.get("soundX", " width - 90");
				y = section.get("soundY", "height - 50");
				soundPos = new Vector2(parse(x), parse(y));
				w = section.get("timeBarLength", "width - 62");
				x = section.get("timeBarX", "33");
				y = section.get("timeBarY", "47");
				timeBar = new Vector3(parse(x), parse(y), parse(w));
				bars = new Texture(Util.getFile(skin, section.get("bars", "colors-borders.png"), useInternal));
				useOldBars = section.get("oldBars", boolean.class, false);
				break;
			default:
				x = section.get("path", "data/particle.png");
				Texture t = new Texture(Util.getFile(skin, x, x.equalsIgnoreCase("data/particle.png")));
				SkinPart sp = new SkinPart(t, parse(section.get("x", "0")), parse(section.get("y", "0")));
				if (section.get("isOnBackground", boolean.class, true)) {
					customPartsBackground.add(sp);
				} else {
					customPartsForeground.add(sp);
				}

			}
		}
	}

	public void rendererCustomParts(SpriteBatch sb, boolean background) {
		ArrayList<SkinPart> workingWith = background ? customPartsBackground : customPartsForeground;
		for (SkinPart sp : workingWith) {
			sp.renderer(sb);
		}
	}

	public static float parse(String exp) {
		Expr expr;
		try {
			expr = Parser.parse(exp.toLowerCase().replaceAll("width", Main.width + "").replaceAll("height", Main.height + ""));
		} catch (SyntaxException e) {
			System.err.println(e.explain());
			return Float.NEGATIVE_INFINITY;
		}
		return (float) expr.value();
	}

	public static Skin getDefaultSkin() throws InvalidFileFormatException, IOException {
		if (defaultSkin == null) {
			defaultSkin = new Skin(Gdx.files.internal("data/default/default.skn").file(), true);
		}
		return defaultSkin;
	}
}
